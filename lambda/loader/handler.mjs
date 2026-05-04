/**
 * S3 → PostgreSQL loader Lambda.
 *
 * Triggered by EventBridge on an hourly schedule. Scans the previous hour's
 * partition prefix for both impressions and clicks, bulk-upserts rows into
 * PostgreSQL, then deletes each processed file.
 *
 * CSV format:  campaign_id,occurred_at,count
 * Key format:  {impressions|clicks}/year={YYYY}/month={MM}/day={DD}/hour={HH}/{HH-MM}-{shardId}.csv.gz
 *
 * Upsert strategy: ON CONFLICT (campaign_id, occurred_at) DO UPDATE SET count = table.count + EXCLUDED.count
 * This handles the rare case where two loader invocations race on the same window.
 */

import { gunzipSync } from 'node:zlib'
import {
  S3Client,
  GetObjectCommand,
  DeleteObjectCommand,
  ListObjectsV2Command,
} from '@aws-sdk/client-s3'
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

// Returns the UTC hour partition prefix for one hour before the given date.
// e.g. for 2024-03-15T14:xx → "impressions/2024/03/15/13"
function buildHourPrefix(dataPrefix, date) {
  const d    = new Date(date)
  d.setUTCHours(d.getUTCHours() - 1)
  const yyyy = d.getUTCFullYear()
  const mm   = String(d.getUTCMonth() + 1).padStart(2, '0')
  const dd   = String(d.getUTCDate()).padStart(2, '0')
  const hh   = String(d.getUTCHours()).padStart(2, '0')
  return `${dataPrefix}/year=${yyyy}/month=${mm}/day=${dd}/hour=${hh}/`
}

async function listPrefix(bucket, prefix) {
  const keys = []
  let token
  do {
    const resp = await s3.send(new ListObjectsV2Command({
      Bucket:            bucket,
      Prefix:            prefix,
      ContinuationToken: token,
    }))
    for (const obj of resp.Contents ?? []) keys.push(obj.Key)
    token = resp.IsTruncated ? resp.NextContinuationToken : undefined
  } while (token)
  return keys
}

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

async function processKey(bucket, key) {
  const table = detectTable(key)
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

export const handler = async () => {
  const now = new Date()

  const prefixes = [
    buildHourPrefix('impressions', now),
    buildHourPrefix('clicks', now),
  ]

  for (const prefix of prefixes) {
    console.log(`Scanning s3://${BUCKET}/${prefix}`)
    const keys = await listPrefix(BUCKET, prefix)
    console.log(`Found ${keys.length} file(s) under ${prefix}`)
    for (const key of keys) {
      await processKey(BUCKET, key)
    }
  }
}
