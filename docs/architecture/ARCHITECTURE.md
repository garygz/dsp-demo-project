# DSP Demo — System Architecture

```mermaid
flowchart LR
    Browser(["👤 Browser\nReact SPA"])

    subgraph AWS ["AWS — us-east-1"]

        ALB["Application Load Balancer\nHTTP · idle_timeout 300s"]

        subgraph ECS ["ECS Fargate — Spring Boot"]
            direction TB
            AUTH["POST /auth/login\nGET  /auth/dev-login\n─────────────────\nJWT · HMAC-SHA512\nSecrets Manager"]

            subgraph CRUD ["Campaign Management"]
                ADV["GET  /advertisers\nPOST /advertisers\n─────────────\nname validation\nuniqueness check"]
                CAMP["GET  /advertisers/:id/campaigns\nPOST /advertisers/:id/campaigns\n─────────────────────────────\nname ≥ 5 chars · landing page URL\nadvertiser FK validation"]
                STATS["GET /advertisers/:id/campaigns/:id/stats\n?from=YYYY-MM-DD&to=YYYY-MM-DD\n────────────────────────────────────\ncountPerDay() · JPA @Query\nreturns impressionsPerDay · clicksPerDay"]
            end

            subgraph INGEST ["High-QPS Data Ingestion"]
                SINGLE["POST /impressions\nPOST /clicks"]
                BATCH["POST /impressions/batch\nPOST /clicks/batch\n────────────────\nup to 25 items / request\nDynamoDB BatchWriteItem"]
                SSE["GET /stats/stream?token=\n──────────────────────\nSSE · SseEmitter\n15s rolling window\nbroadcast to subscribers"]
            end
        end

        subgraph Storage ["Storage"]
            RDS[("PostgreSQL 16\n───────────────\nadvertisers\ncampaigns\nimpression_events\nclick_events")]
            DDB[("DynamoDB\n───────────────\nimpressions\nPK: campaignId\nSK: timestamp\n───────────────\nclicks\nPK: campaignId\nSK: timestamp")]
        end

    end

    %% Request flow
    Browser -->|"JWT in\nAuthorization header"| ALB
    ALB --> AUTH
    ALB --> ADV
    ALB --> CAMP
    ALB --> STATS
    ALB --> SINGLE
    ALB --> BATCH
    ALB -->|"?token= query param\n(EventSource limitation)"| SSE

    %% Storage access
    AUTH -.->|"validate secret\non startup"| AWS
    ADV -->|"JPA"| RDS
    CAMP -->|"JPA"| RDS
    STATS -->|"JPA · @Query"| RDS
    SINGLE -->|"PutItem"| DDB
    BATCH -->|"BatchWriteItem\n≤ 25 items / call"| DDB
    SSE -->|"Query\n15s window"| DDB

    %% Styles
    classDef alb     fill:#FF9900,color:#000,stroke:#c97500
    classDef ecs     fill:#e8740c,color:#fff,stroke:#b35a00
    classDef rds     fill:#3b48cc,color:#fff,stroke:#2a35a0
    classDef ddb     fill:#3b48cc,color:#fff,stroke:#2a35a0
    classDef actor   fill:#232f3e,color:#fff,stroke:#000

    class ALB alb
    class AUTH,ADV,CAMP,STATS,SINGLE,BATCH,SSE ecs
    class RDS rds
    class DDB ddb
    class Browser actor
```

## Components

### Application Load Balancer
Single entry point for all traffic. Idle timeout raised to 300 seconds to keep SSE connections alive across the 15-second tick interval. Routes all requests to the single ECS Fargate task.

### Authentication
Stateless JWT issued on login. Every protected endpoint runs through `JwtAuthFilter` which reads the token from the `Authorization: Bearer` header — or from the `?token=` query parameter for SSE connections, since `EventSource` does not support custom headers.

### Campaign Management (CRUD → PostgreSQL)
| Endpoint | Description |
|---|---|
| `GET /advertisers` | List all advertisers |
| `POST /advertisers` | Create advertiser — name validation, uniqueness check |
| `GET /advertisers/:id/campaigns` | List campaigns for an advertiser |
| `POST /advertisers/:id/campaigns` | Create campaign — name ≥ 5 chars, landing page URL, advertiser FK |
| `GET /advertisers/:id/campaigns/:id/stats` | Daily impression + click counts from PostgreSQL aggregates |

All CRUD operations persist to **PostgreSQL** via Spring Data JPA. Stats are read from pre-aggregated `impression_events` and `click_events` tables populated by the ingestion pipeline.

### High-QPS Data Ingestion (→ DynamoDB)
Ad events bypass the relational database entirely and write directly to **DynamoDB** for low-latency, high-throughput ingest.

| Endpoint | Write pattern |
|---|---|
| `POST /impressions` | Single `PutItem` |
| `POST /clicks` | Single `PutItem` |
| `POST /impressions/batch` | `BatchWriteItem` in chunks of 25 (DynamoDB limit) |
| `POST /clicks/batch` | `BatchWriteItem` in chunks of 25 |

Both tables use `campaignId` as the partition key and `timestamp` (ISO-8601) as the sort key. This co-locates all events for a campaign on the same partition and makes range queries — `WHERE timestamp BETWEEN :from AND :to` — efficient without a scan.

### Live Streaming (SSE → DynamoDB)
`GET /stats/stream` opens a persistent SSE connection. A `@Scheduled` task fires every 15 seconds, queries DynamoDB for events in the last 15-second window per active subscriber, and broadcasts a `tick` event with `{ occurredAt, impressions, clicks }`. A keep-alive comment is sent every 20 seconds to prevent the ALB from closing idle connections.

---

> **Data ingestion pipeline** — for a detailed breakdown of how raw DynamoDB events are aggregated by Lambda, written to S3, and loaded into PostgreSQL via EventBridge, see [DATA_INGESTION_ARCHITECTURE.md](diagrams/DATA_INGESTION_ARCHITECTURE.md).
