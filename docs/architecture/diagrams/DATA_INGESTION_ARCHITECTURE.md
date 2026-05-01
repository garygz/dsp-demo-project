# DSP Demo — Production Architecture

```mermaid
flowchart TB
    %% ── Actors ───────────────────────────────────────────────────────────────
    Browser(["👤 Browser"])

    %% ── Frontend ─────────────────────────────────────────────────────────────
    subgraph S3_UI ["S3 — Static Hosting (HTTP)"]
        UI["React SPA\n(Vite build)"]
    end

    %% ── Networking ───────────────────────────────────────────────────────────
    subgraph Public ["Public Subnets"]
        ALB["Application Load Balancer\nidle_timeout = 300s"]
    end

    %% ── Compute ──────────────────────────────────────────────────────────────
    subgraph Private ["Private Subnets"]
        ECS["ECS Fargate\nSpring Boot\n0.5 vCPU · 1 GB"]
        LAMBDA_LOAD["Loader Lambda\nNode.js 20\nbatch upsert → RDS"]
        RDS[("PostgreSQL 16\nRDS db.t3.micro\n─────────────\nadvertisers\ncampaigns\nimpression_events\nclick_events")]
    end

    %% ── DynamoDB ─────────────────────────────────────────────────────────────
    subgraph Dynamo ["DynamoDB — PAY_PER_REQUEST"]
        IMP[("impressions\nPK: campaignId\nSK: timestamp")]
        CLK[("clicks\nPK: campaignId\nSK: timestamp")]
    end

    %% ── Aggregation pipeline ─────────────────────────────────────────────────
    subgraph Pipeline ["Aggregation Pipeline"]
        LAMBDA_AGG["Aggregator Lambda\nNode.js 20\n60s tumbling window"]
        S3_CSV[("S3 Bucket\naggregated CSV\n(gzipped, 90d TTL)")]
        EB["EventBridge\nS3 ObjectCreated rule"]
    end

    %% ── Security / Config ────────────────────────────────────────────────────
    SM(["Secrets Manager\nJWT secret"])
    SSM(["SSM Parameter Store\nDB credentials"])

    %% ── CI/CD ────────────────────────────────────────────────────────────────
    subgraph CICD ["CI/CD"]
        direction LR
        GH["GitHub\nmain branch"]
        CP_UI["CodePipeline UI\nCodeBuild → S3 sync"]
        CP_BE["CodePipeline Backend\nCodeBuild → ECR → ECS"]
    end

    %% ── Flows ────────────────────────────────────────────────────────────────

    %% User loads the SPA
    Browser -->|"HTTP GET"| UI

    %% REST API calls
    Browser -->|"REST · JWT\nGET /advertisers\nGET /campaigns\nGET /stats"| ALB
    ALB --> ECS

    %% Live streaming (SSE)
    Browser -->|"SSE · ?token=JWT\nGET /stats/stream"| ALB

    %% Spring Boot → DynamoDB (high-frequency ingest)
    ECS -->|"BatchWriteItem\n≤25 items / request"| IMP
    ECS -->|"BatchWriteItem\n≤25 items / request"| CLK

    %% Spring Boot → RDS (campaign management + stats queries)
    ECS -->|"JPA · JDBC\ncampaigns · advertisers\ncountPerDay()"| RDS

    %% Live stats: Spring Boot polls DynamoDB every 15s per subscriber
    ECS -->|"Query\n15s rolling window\n→ SSE tick"| IMP
    ECS -->|"Query\n15s rolling window\n→ SSE tick"| CLK

    %% DynamoDB Streams → Aggregator Lambda
    IMP -->|"DynamoDB Stream\nNEW_IMAGE"| LAMBDA_AGG
    CLK -->|"DynamoDB Stream\nNEW_IMAGE"| LAMBDA_AGG

    %% Aggregator → S3
    LAMBDA_AGG -->|"gzipped CSV\nper 60s window"| S3_CSV

    %% S3 → EventBridge → Loader Lambda
    S3_CSV -->|"ObjectCreated event"| EB
    EB -->|"invoke"| LAMBDA_LOAD

    %% Loader Lambda → RDS
    LAMBDA_LOAD -->|"UPSERT\nimpression_events\nclick_events"| RDS

    %% Secrets
    ECS -.->|"startup"| SM
    ECS -.->|"startup"| SSM
    LAMBDA_LOAD -.->|"env vars"| SSM

    %% CI/CD
    GH -->|"push to main"| CP_UI
    GH -->|"push to main"| CP_BE
    CP_UI -->|"npm build\naws s3 sync"| UI
    CP_BE -->|"docker build\nECR push\nECS deploy"| ECS

    %% ── Styles ───────────────────────────────────────────────────────────────
    classDef aws        fill:#FF9900,color:#000,stroke:#c97500
    classDef db         fill:#3b48cc,color:#fff,stroke:#2a35a0
    classDef lambda     fill:#e8740c,color:#fff,stroke:#b35a00
    classDef storage    fill:#3f8624,color:#fff,stroke:#2d6119
    classDef infra      fill:#8c4fff,color:#fff,stroke:#6a33cc
    classDef actor      fill:#232f3e,color:#fff,stroke:#000

    class ALB,ECS aws
    class RDS,IMP,CLK db
    class LAMBDA_AGG,LAMBDA_LOAD lambda
    class S3_CSV,UI storage
    class SM,SSM,EB infra
    class Browser actor
```

## Data Flow Summary

| Path | Latency | Storage |
|---|---|---|
| Load Generator → Impressions/Clicks | Real-time | DynamoDB (raw events) |
| DynamoDB Stream → Aggregator | ~60s tumbling window | S3 (gzipped CSV) |
| S3 ObjectCreated → Loader → RDS | Seconds after CSV lands | PostgreSQL (daily aggregates) |
| Live chart (SSE) | 15s polling window | DynamoDB (direct query) |
| Historical chart (REST) | On demand | PostgreSQL (countPerDay) |
