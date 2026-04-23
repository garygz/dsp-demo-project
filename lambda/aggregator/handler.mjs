/**
 * DynamoDB Streams → Lambda Tumbling Window aggregator.
 *
 * Deployed with two event source mappings (one per table):
 *   - impressions table stream  →  writes impressions/ prefix to S3
 *   - clicks table stream       →  writes clicks/ prefix to S3
 *
 * Within each 1-minute tumbling window Lambda is invoked repeatedly with partial
 * batches from the stream. Each invocation accumulates impression/click counts per
 * campaign into `state`, which Lambda preserves across invocations in the window.
 * On the final invocation the aggregated counts are written as a CSV to S3 and
 * state is reset. A separate loader Lambda picks up the file and upserts into PostgreSQL.
 *
 * State shape:  { [campaignId]: number }
 *
 * S3 key format: {impressions|clicks}/{YYYY}/{MM}/{DD}/{HH-MM-SS}.csv
 */

import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3'

const s3 = new S3Client({ region: process.env.AWS_REGION ?? 'us-east-1' })

const BUCKET = process.env.S3_BUCKET

const detectPrefix = (eventSourceARN = '') =>
  eventSourceARN.includes('/clicks/') ? 'clicks' : 'impressions'

const buildS3Key = (prefix, windowStart) => {
  const d = new Date(windowStart)
  const yyyy = d.getUTCFullYear()
  const mm   = String(d.getUTCMonth() + 1).padStart(2, '0')
  const dd   = String(d.getUTCDate()).padStart(2, '0')
  const time = d.toISOString().slice(11, 19).replace(/:/g, '-') // HH-MM-SS
  return `${prefix}/${yyyy}/${mm}/${dd}/${time}.csv`
}

const buildCSV = (state, occurredAt) => {
  const header = 'campaign_id,occurred_at,count'
  const lines  = Object.entries(state).map(([campaignId, count]) =>
    `${campaignId},${occurredAt},${count}`
  )
  return [header, ...lines].join('\n')
}

export const handler = async (event) => {
  const {
    Records = [],
    state = {},
    isFinalInvokeForWindow,
    isWindowTerminatedEarlyForBatchFailure,
    window: tumblingWindow,
    eventSourceARN,
  } = event

  // Accumulate counts for this batch
  const updatedState = { ...state }
  for (const record of Records) {
    if (record.eventName !== 'INSERT') continue
    const campaignId = record.dynamodb?.NewImage?.campaignId?.S
    if (campaignId) {
      updatedState[campaignId] = (updatedState[campaignId] ?? 0) + 1
    }
  }

  // Non-final invocation — return updated state for next batch
  if (!isFinalInvokeForWindow && !isWindowTerminatedEarlyForBatchFailure) {
    return { state: updatedState }
  }

  // Final invocation — write CSV to S3
  if (Object.keys(updatedState).length > 0) {
    const prefix    = detectPrefix(eventSourceARN)
    const occurredAt = tumblingWindow.start
    const key       = buildS3Key(prefix, occurredAt)
    const csv       = buildCSV(updatedState, occurredAt)

    await s3.send(new PutObjectCommand({
      Bucket:      BUCKET,
      Key:         key,
      Body:        csv,
      ContentType: 'text/csv',
    }))

    console.log(`Window ${tumblingWindow.start} → ${tumblingWindow.end}: wrote ${Object.keys(updatedState).length} rows to s3://${BUCKET}/${key}`)
  }

  return { state: {} }
}
