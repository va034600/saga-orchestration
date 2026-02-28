# Saga Orchestration

Spring Boot + Kotlin によるSagaオーケストレーションパターンのマイクロサービス実装。
注文と決済の2サービス構成。同期 / 非同期（Step Functions）の2つの実行モードを提供。

## 技術スタック

| 項目 | バージョン |
|---|---|
| Spring Boot | 3.5.3 |
| Kotlin | 2.1.0 |
| Java | 17 |
| Gradle | 8.12 (Kotlin DSL) |
| PostgreSQL | 16 |
| Flyway | 11.3.1 |
| LocalStack | 3.8 (SQS, EventBridge, Step Functions) |
| Spring Cloud AWS | 3.3.1 |
| SpringDoc OpenAPI | 2.8.0 |
| Micrometer Tracing | OpenTelemetry Bridge |

## 業務フロー

### 同期Saga (`POST /api/saga/orders`)

Orchestrator が各サービスを順に呼び出し、結果を待って返す。

```
Client
  │ POST /api/saga/orders
  ▼
Orchestrator (:8080)
  │
  ├─ 1. CreateOrder (PENDING)     → order-service
  ├─ 2. ExecutePayment            → payment-service (authorize + capture)
  ├─ 3. CompleteOrder (COMPLETED) → order-service
  │
  └─ 200 OK  ← Client
```

失敗時は Transactional Outbox → EventBridge → SQS → compensation-service で非同期補償:

- 決済済みの場合: RefundPayment → CancelOrder
- 注文のみ作成済みの場合: CancelOrder

補償イベントは `@Transactional` 内で `outbox_events` テーブルに書き込まれ、ポーラーが非同期に EventBridge へ発行する（at-least-once 保証）。

### 非同期Saga (`POST /api/saga/orders/async`)

Step Functions がステートマシンとして各サービスを呼び出す。クライアントには `executionArn` を即返却（202）。

```
Client
  │ POST /api/saga/orders/async
  ▼
Orchestrator (:8080)
  │ startExecution
  ▼
Step Functions (order-saga)
  │
  ├─ CreateOrder      → order-service
  ├─ ExecutePayment   → payment-service /authorize
  ├─ CapturePayment   → payment-service /capture
  ├─ CompleteOrder     → order-service
  └─ Succeed

  失敗時（自動補償）:
  ├─ CompensateRefundPayment → payment-service /refund
  ├─ CompensateCancelOrder   → order-service /cancel
  └─ Fail
```

実行状態は `GET /api/saga/executions?executionArn=...` で確認。

## モジュール構成

```
saga-orchestration/
├── services/                        # 言語非依存リソース
│   ├── common/
│   │   └── openapi.yml              #   共有 DTO 定義
│   ├── order-service/
│   │   ├── openapi.yml              #   API 定義
│   │   └── db/migration/            #   Flyway マイグレーション SQL
│   ├── payment-service/
│   │   ├── openapi.yml
│   │   └── db/migration/
│   ├── compensation-service/
│   │   └── db/migration/
│   └── orchestrator/
│       ├── openapi.yml
│       └── db/migration/
├── kotlin/                          # Kotlin + Spring Boot 実装（Gradle ルートプロジェクト）
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew / gradlew.bat
│   ├── common/                      # 共通DTO, Enum, 例外, 冪等性AOP, トレーシング
│   ├── order-service/               # 注文管理 (:8081)
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   │   └── persistence/         # JPA エンティティ + リポジトリ実装
│   │   └── bootstrap/
│   ├── payment-service/             # 決済管理 (:8083)
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   │   └── persistence/         # JPA エンティティ + リポジトリ実装
│   │   └── bootstrap/
│   ├── compensation-service/        # 非同期補償処理 (:8084)
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   │   ├── persistence/         # JPA エンティティ + リポジトリ実装
│   │   │   ├── messaging/           # SQS リスナー
│   │   │   └── http/                # REST クライアント + Config
│   │   └── bootstrap/
│   └── orchestrator/                # Sagaオーケストレーター (:8080)
│       ├── domain/
│       ├── application/
│       ├── infrastructure/
│       │   ├── persistence/         # JPA エンティティ + リポジトリ実装
│       │   ├── http/                # WebClient ベースの HTTP クライアント
│       │   ├── messaging/           # EventBridge パブリッシャー
│       │   └── aws/                 # Step Functions クライアント
│       └── bootstrap/
└── docker/
    ├── docker-compose.yml           # PostgreSQL + LocalStack
    ├── postgres/                    # DB初期化スクリプト (docker-entrypoint-initdb.d)
    └── localstack/
        ├── init-aws.sh              # LocalStack起動時に自動実行
        └── state-machine.json       # Step Functions ステートマシン定義
```

`infrastructure` モジュールは技術関心ごとにサブモジュール化されており、不要な依存の混入をコンパイル時に防止する（例: persistence モジュールに messaging 依存が入らない）。

```
bootstrap → infrastructure:persistence  → domain           (+ JPA)
          → infrastructure:http         → application      (+ Web/WebFlux)
          → infrastructure:messaging    → application      (+ SQS/EventBridge)
          → infrastructure:aws          → application      (+ SFN SDK)
          → application → domain
          → common
```

## 各サービスの責務

### order-service (:8081)

| エンドポイント | 説明 |
|---|---|
| `POST /api/orders` | 注文作成 (PENDING) |
| `GET /api/orders/{orderId}` | 注文取得 |
| `PUT /api/orders/{orderId}/complete` | 注文完了 (COMPLETED) |
| `PUT /api/orders/{orderId}/cancel` | 注文キャンセル (CANCELLED) |

### payment-service (:8083)

2段階決済: authorize（仮確保）→ capture（確定）

| エンドポイント | 説明 |
|---|---|
| `POST /api/payments/authorize` | 決済認可 |
| `PUT /api/payments/{orderId}/capture` | 決済確定 |
| `PUT /api/payments/{orderId}/refund` | 返金 |

### orchestrator (:8080)

| エンドポイント | 説明 |
|---|---|
| `POST /api/saga/orders` | 同期Saga実行 |
| `POST /api/saga/orders/async` | 非同期Saga実行（202 + executionArn） |
| `GET /api/saga/executions?executionArn=...` | 非同期Sagaの実行状態取得 |

### compensation-service (:8084)

SQSキューからの補償イベントを受信し、各サービスの補償エンドポイントを呼び出す（同期Sagaの失敗時に使用）。

- `RefundPayment` → payment-service
- `CancelOrder` → order-service

SQS 受信時に `compensation` レコードと `outbox_tasks` を同一トランザクションで保存し、ポーラーが非同期に HTTP 補償を実行する（Transactional Outbox Pattern）。

## Transactional Outbox Pattern

DB トランザクションと外部イベント発行の原子性を保証するために、orchestrator と compensation-service に Transactional Outbox Pattern を適用している。

```
[orchestrator]
@Transactional {
  saga_states 更新 + outbox_events INSERT
}
  ↓ (5秒ポーリング)
OutboxPublisher → EventBridge → SQS → compensation-service

[compensation-service]
@Transactional {
  compensations INSERT + outbox_tasks INSERT
}
  ↓ (5秒ポーリング)
OutboxTaskPublisher → HTTP (refund / cancel)
```

- 外部呼び出しの意図が DB に記録されるため、クラッシュ時もポーラー再開でリトライされる
- 発行失敗時は後続をスキップし順序を保証。次回ポーリングで再試行（at-least-once）
- 受信側は冪等性キー（`Idempotency-Key` ヘッダー）で二重実行を防止

## 横断的機能

### 冪等性

すべての更新系APIは `Idempotency-Key` ヘッダーで二重実行を防止。

```
POST /api/orders
Idempotency-Key: create-order-ORD-001
```

`@Idempotent` アノテーション + AOP Aspect で透過的に制御。

### 分散トレーシング

`X-Trace-Id` ヘッダーで全サービス間のリクエストを追跡。MDCに伝搬しログに出力。

## セットアップ

### 前提条件

- Java 17+
- Docker / Docker Compose

### 1. インフラ起動

```bash
docker-compose -f docker/docker-compose.yml up -d
```

> **注意**: `docker/postgres/init-db.sh` は PostgreSQL の `docker-entrypoint-initdb.d` で実行されるため、**ボリューム初回作成時のみ**動作します。DB を再作成するには `docker-compose -f docker/docker-compose.yml down -v && docker-compose -f docker/docker-compose.yml up -d` を実行してください。

PostgreSQL 16 と LocalStack 3.8 が起動し、以下が自動作成される:

- **DB**: `order_db`, `payment_db`, `compensation_db`, `orchestrator_db`
- **SQS**: `compensation-queue`
- **EventBridge**: `saga-events` バス + `compensation-rule`
- **Step Functions**: `order-saga` ステートマシン

### 2. ビルド

```bash
cd kotlin
./gradlew build
```

### 3. 各サービス起動

```bash
cd kotlin
./gradlew :orchestrator:bootstrap:bootRun
./gradlew :order-service:bootstrap:bootRun
./gradlew :payment-service:bootstrap:bootRun
./gradlew :compensation-service:bootstrap:bootRun
```

## 動作確認

### LocalStack の起動確認

```bash
# ヘルスチェック
curl http://localhost:4566/_localstack/health

# SQS キュー確認
aws --endpoint-url=http://localhost:4566 sqs list-queues

# EventBridge バス確認
aws --endpoint-url=http://localhost:4566 events list-event-buses

# Step Functions ステートマシン確認
aws --endpoint-url=http://localhost:4566 stepfunctions list-state-machines
```

> `awslocal` がインストール済みの場合は `aws --endpoint-url=...` の代わりに `awslocal` コマンドが使えます。

### PostgreSQL の起動確認

```bash
docker exec postgres psql -U saga -d saga -tc "SELECT datname FROM pg_database WHERE datname LIKE '%_db'"
```

### ヘルスチェック

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

### 同期Saga

```bash
curl -X POST http://localhost:8080/api/saga/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: saga-ORD-001" \
  -d '{
    "orderId": "ORD-001",
    "productId": "PROD-001",
    "quantity": 2,
    "amount": 5000
  }'
```

### 非同期Saga

```bash
# 実行開始（202 Accepted）
curl -X POST http://localhost:8080/api/saga/orders/async \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: async-saga-ORD-002" \
  -d '{
    "orderId": "ORD-002",
    "productId": "PROD-001",
    "quantity": 1,
    "amount": 3000
  }'

# 実行状態確認
curl "http://localhost:8080/api/saga/executions?executionArn=arn:aws:states:ap-northeast-1:000000000000:execution:order-saga:saga-ORD-002"
```

## DB スキーマ

各サービスのマイグレーションは Flyway で管理（`services/<service>/db/migration/`）。ビルド時に `processResources` で Kotlin プロジェクトにコピーされる。

| サービス | テーブル |
|---|---|
| order-service | `orders`, `idempotency_keys` |
| payment-service | `payments`, `idempotency_keys` |
| orchestrator | `saga_states`, `saga_steps`, `outbox_events`, `idempotency_keys` |
| compensation-service | `compensations`, `outbox_tasks`, `idempotency_keys` |

## OpenAPI / Swagger UI

API 定義は `services/` 配下に言語非依存リソースとして配置:

- `services/order-service/openapi.yml`
- `services/payment-service/openapi.yml`
- `services/orchestrator/openapi.yml`

共通スキーマ（コード生成用）: `services/common/openapi.yml`

全サービスの API をまとめて閲覧できる Swagger UI を提供（ドロップダウンでサービスを切り替え）:

| 方法 | URL | 備考 |
|------|-----|------|
| GitHub Pages | https://va034600.github.io/saga-orchestration/ | main push 時に自動デプロイ |
| Docker Compose | http://localhost:8090 | `docker compose -f docker/docker-compose.yml up swagger-ui -d` |

各サービス単体の Swagger UI も利用可能（サービス起動後）:

| サービス | Swagger UI |
|---------|-----------|
| orchestrator | http://localhost:8080/swagger-ui.html |
| order-service | http://localhost:8081/swagger-ui.html |
| payment-service | http://localhost:8083/swagger-ui.html |
| compensation-service | http://localhost:8084/swagger-ui.html |

## スキーマドキュメント（tbls）

[tbls](https://github.com/k1LoW/tbls) で各サービスの DB スキーマドキュメント（ER 図 + テーブル定義）を自動生成し、GitHub Pages で公開している。

| サービス | GitHub Pages |
|---------|-------------|
| orchestrator | https://va034600.github.io/saga-orchestration/schema/orchestrator/ |
| order-service | https://va034600.github.io/saga-orchestration/schema/order-service/ |
| payment-service | https://va034600.github.io/saga-orchestration/schema/payment-service/ |
| compensation-service | https://va034600.github.io/saga-orchestration/schema/compensation-service/ |

### ローカルで閲覧

Docker Compose でマイグレーション適用 + ドキュメント生成 + HTTP サーバーが起動する:

```bash
docker compose -f docker/docker-compose.yml up tbls -d
```

http://localhost:3000 で閲覧可能。

### ローカルで生成（tbls インストール済みの場合）

Docker PostgreSQL 起動中に:

```bash
tbls doc --rm-dist -c services/orchestrator/.tbls.yml
tbls doc --rm-dist -c services/order-service/.tbls.yml
tbls doc --rm-dist -c services/payment-service/.tbls.yml
tbls doc --rm-dist -c services/compensation-service/.tbls.yml
```

生成先は各 `services/<service>/docs/` ディレクトリ。
