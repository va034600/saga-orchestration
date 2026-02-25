# Saga Orchestration

Spring Boot + Kotlin によるSagaオーケストレーションパターンのマイクロサービス実装。
注文と決済の2サービス構成。

## 技術スタック

| 項目 | バージョン |
|---|---|
| Spring Boot | 3.5.3 |
| Kotlin | 2.1.0 |
| Java | 17 |
| Gradle | 8.12 (Kotlin DSL) |
| PostgreSQL | 16 |
| Flyway | 11.3.1 |
| LocalStack | 3.8 (SQS, EventBridge) |
| Spring Cloud AWS | 3.3.1 |
| SpringDoc OpenAPI | 2.8.0 |
| Micrometer Tracing | OpenTelemetry Bridge |

## 業務フロー

### 正常フロー（同期）

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

### 異常フロー（決済失敗 → 非同期補償）

```
Client
  │ POST /api/saga/orders
  ▼
Orchestrator (:8080)
  │
  ├─ 1. CreateOrder (PENDING)  → order-service  ✅
  ├─ 2. ExecutePayment         → payment-service ❌ 失敗
  │
  ├─ CompensationEvent 発行    → EventBridge (saga-events)
  └─ 422 Error  ← Client
                                     │
                                     ▼
                               SQS (compensation-queue)
                                     │
                                     ▼
                               Compensation Service (:8084)
                                 → CancelOrder (CANCELLED)
```

完了ステップに応じて補償イベントを発行:

- 決済済みの場合: RefundPayment → CancelOrder
- 注文のみ作成済みの場合: CancelOrder

## モジュール構成

```
saga-orchestration/
├── common/                  # 共通DTO, Enum, 例外, 冪等性AOP, トレーシング
├── order-service/           # 注文管理 (:8081)
├── payment-service/         # 決済管理 (:8083)
├── compensation-service/    # 非同期補償処理 (:8084)
├── orchestrator/            # Sagaオーケストレーター (:8080)
├── docker-compose.yml       # PostgreSQL + LocalStack
├── localstack/              # AWS初期化スクリプト
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
| `POST /api/saga/orders` | Saga実行 (同期) |

### compensation-service (:8084)

SQSキューからの補償イベントを受信し、各サービスの補償エンドポイントを呼び出す。

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

PostgreSQL 16 と LocalStack 3.8 が起動し、以下が自動作成される:

- **DB**: `order_db`, `payment_db`, `compensation_db`, `orchestrator_db`
- **SQS**: `compensation-queue`
- **EventBridge**: `saga-events` バス + `compensation-rule`

### 2. ビルド

```bash
./gradlew build
```

### 3. 各サービス起動

```bash
./gradlew :orchestrator:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :compensation-service:bootRun
```

### 4. ヘルスチェック

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

## 動作確認

### 注文Saga実行

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
