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

失敗時は EventBridge → SQS → compensation-service で非同期補償:

- 決済済みの場合: RefundPayment → CancelOrder
- 注文のみ作成済みの場合: CancelOrder

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
├── common/                  # 共通DTO, Enum, 例外, 冪等性AOP, トレーシング
├── order-service/           # 注文管理 (:8081)
├── payment-service/         # 決済管理 (:8083)
├── compensation-service/    # 非同期補償処理 (:8084)
├── orchestrator/            # Sagaオーケストレーター (:8080)
├── docker-compose.yml       # PostgreSQL + LocalStack
├── postgres/                # DB初期化スクリプト (docker-entrypoint-initdb.d)
├── localstack/
│   ├── init-aws.sh          # LocalStack起動時に自動実行: SQS, EventBridge, Step Functions作成
│   └── state-machine.json   # Step Functions ステートマシン定義 (非同期Saga)
└── docs/openapi/            # OpenAPI仕様
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
docker-compose up -d
```

> **注意**: `postgres/init-db.sh` は PostgreSQL の `docker-entrypoint-initdb.d` で実行されるため、**ボリューム初回作成時のみ**動作します。DB を再作成するには `docker-compose down -v && docker-compose up -d` を実行してください。

PostgreSQL 16 と LocalStack 3.8 が起動し、以下が自動作成される:

- **DB**: `order_db`, `payment_db`, `compensation_db`, `orchestrator_db`
- **SQS**: `compensation-queue`
- **EventBridge**: `saga-events` バス + `compensation-rule`
- **Step Functions**: `order-saga` ステートマシン

### 2. LocalStack の起動確認

```bash
# ヘルスチェック
curl http://localhost:4566/_localstack/health

# SQS キュー確認
aws --endpoint-url=http://localhost:4566 sqs list-queues

# EventBridge バス確認
aws --endpoint-url=http://localhost:4566 events list-event-buses

# Step Functions ステートマシン確認
aws --endpoint-url=http://localhost:4566 stepfunctions list-state-machines

# PostgreSQL DB一覧確認
docker exec postgres psql -U saga -d saga -tc "SELECT datname FROM pg_database WHERE datname LIKE '%_db'"
```

> `awslocal` がインストール済みの場合は `aws --endpoint-url=...` の代わりに `awslocal` コマンドが使えます。

### 3. ビルド

```bash
./gradlew build
```

### 4. 各サービス起動

```bash
./gradlew :orchestrator:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :compensation-service:bootRun
```

### 5. ヘルスチェック

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

## 動作確認

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

各サービスのマイグレーションは Flyway で管理 (`src/main/resources/db/migration/`)。

| サービス | テーブル |
|---|---|
| order-service | `orders`, `idempotency_keys` |
| payment-service | `payments`, `idempotency_keys` |
| orchestrator | `saga_states`, `saga_steps`, `idempotency_keys` |
| compensation-service | `compensations` |

## OpenAPI

API仕様は `docs/openapi/` に配置:

- `order-service.yml`
- `payment-service.yml`
- `orchestrator.yml`
- `compensation-service.yml`
