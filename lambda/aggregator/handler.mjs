/**
 * DynamoDB Streams → Lambda Tumbling Window aggregator.
 *
 * Deployed with two event source mappings (one per table):
 *   - impressions table stream  →  writes to impression_events
 *   - clicks table stream       →  writes to click_events
 *
 * Within each 1-minute tumbling window Lambda is invoked repeatedly with partial
 * batches from the stream. Each invocation accumulates impression/click counts per
 * campaign into `state`, which Lambda preserves across invocations in the window.
 * On the final invocation (`isFinalInvokeForWindow = true`) the aggregated counts
 * are flushed to PostgreSQL and state is reset.
 *
 * State shape:  { [campaignId]: number }
 */

import pg from 'pg'

const { Pool } = pg

// Pool is reused across invocations within the same Lambda execution environment.
// It is only connected on the final invocation of each window.
let pool

const getPool = () => {
  if (!pool) {
    pool = new Pool({
      host:     process.env.POSTGRES_HOST,
      port:     parseInt(process.env.POSTGRES_PORT ?? '5432'),
      database: process.env.POSTGRES_DB,
      user:     process.env.POSTGRES_USER,
      password: process.env.POSTGRES_PASSWORD,
    })
  }
  return pool
}

const detectTable = (eventSourceARN = '') =>
  eventSourceARN.includes('/clicks/') ? 'click_events' : 'impression_events'

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

  // Final invocation — flush aggregated counts to PostgreSQL
  const pgTable  = detectTable(eventSourceARN)
  const occurredAt = new Date(tumblingWindow.start).toISOString()

  // One row per campaign containing the total count for this window
  const rows = []
  for (const [campaignId, count] of Object.entries(updatedState)) {
    rows.push([campaignId, occurredAt, count])
  }

  if (rows.length > 0) {
    const client = await getPool().connect()
    try {
      const placeholders = rows
        .map((_, i) => {
          const currentRowToColOffset = i * 3
          return `($${currentRowToColOffset + 1}, $${currentRowToColOffset + 2}, $${currentRowToColOffset + 3})`
        })
        .join(', ')
      await client.query(
        `INSERT INTO ${pgTable} (campaign_id, occurred_at, count)
         VALUES ${placeholders}
         ON CONFLICT (campaign_id, occurred_at) DO UPDATE SET count = ${pgTable}.count + EXCLUDED.count`,
        rows.flat()
      )
      console.log(
        `Window ${tumblingWindow.start} → ${tumblingWindow.end}: ` +
        `inserted ${rows.length} rows into ${pgTable}`
      )
    } finally {
      client.release()
    }
  }

  return { state: {} }
}
