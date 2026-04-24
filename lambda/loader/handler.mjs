/**
 * S3 → PostgreSQL loader Lambda.
 *
 * Triggered by S3 PUT notifications from the aggregator Lambda.
 * Reads a gzipped CSV file, bulk-upserts rows into impression_events or click_events,
 * then deletes the file from S3.
 *
 * CSV format:  campaign_id,occurred_at,count
 * Key format:  {impressions|clicks}/{YYYY}/{MM}/{DD}/{HH}/{HH-MM}-{shardId}.csv.gz
 *
 * Upsert strategy: ON CONFLICT (campaign_id, occurred_at) DO UPDATE SET count = table.count + EXCLUDED.count
 * This handles the rare case where two loader invocations race on the same window.
 */

import { gunzipSync } from 'node:zlib'
import { S3Client, GetObjectCommand, DeleteObjectCommand } from '@aws-sdk/client-s3'
import pg from 'pg'

const s3 = new S3Client({ region: process.env.AWS_REGION ?? 'us-east-1' })

const BUCKET = process.env.S3_BUCKET

// Lazy pool — reused across warm invocations
let pool

function getPool() {
  if (!pool) {
    pool = new pg.Pool({
      host:     process.env.PG_HOST,
      port:     Number(process.env.PG_PORT ?? 5432),
      database: process.env.PG_DATABASE,
      user:     process.env.PG_USER,
      password: process.env.PG_PASSWORD,
      max:      2,
      ssl:      process.env.PG_SSL === 'true' ? { rejectUnauthorized: false } : false,
    })
  }
  return pool
}

const detectTable = (key = '') =>
  key.startsWith('clicks/') ? 'click_events' : 'impression_events'

async function readS3Object(bucket, key) {
  const { Body } = await s3.send(new GetObjectCommand({ Bucket: bucket, Key: key }))
  const compressed = await Body.transformToByteArray()
  return gunzipSync(compressed).toString('utf-8')
}

function parseCSV(csv) {
  const [_header, ...lines] = csv.trim().split('\n')
  return lines
    .map(line => line.trim())
    .filter(Boolean)
    .map(line => {
      const [campaignId, occurredAt, count] = line.split(',')
      return { campaignId, occurredAt, count: Number(count) }
    })
}

async function upsertRows(table, rows) {
  if (rows.length === 0) return

  // Build parameterised VALUES list: ($1,$2,$3), ($4,$5,$6), ...
  const values = []
  const placeholders = rows.map((row, i) => {
    const offset = i * 3
    values.push(row.campaignId, row.occurredAt, row.count)
    return `($${offset + 1}, $${offset + 2}, $${offset + 3})`
  })

  const sql = `
    INSERT INTO ${table} (campaign_id, occurred_at, count)
    VALUES ${placeholders.join(', ')}
    ON CONFLICT (campaign_id, occurred_at)
    DO UPDATE SET count = ${table}.count + EXCLUDED.count
  `

  await getPool().query(sql, values)
}

export const handler = async (event) => {
  for (const record of event.Records) {
    const bucket = record.s3.bucket.name
    const key    = decodeURIComponent(record.s3.object.key.replace(/\+/g, ' '))
    const table  = detectTable(key)

    console.log(`Processing s3://${bucket}/${key} → ${table}`)

    const csv  = await readS3Object(bucket, key)
    const rows = parseCSV(csv)

    if (rows.length > 0) {
      await upsertRows(table, rows)
      console.log(`Upserted ${rows.length} rows into ${table}`)
    }

    await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key }))
    console.log(`Deleted s3://${bucket}/${key}`)
  }
}
