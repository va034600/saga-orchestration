- このサービスはマイクロサービスで設計する
- Kotlin アプリは DDD で実装する（Go はフラット構成）
- 言語: Kotlin + Spring Boot / Go
- ビルド:
  - Kotlin: `./gradlew clean build`（kotlin/ ディレクトリで実行）
  - Go: `go build ./...`（go/payment-service/ ディレクトリで実行）

## ディレクトリ構成

```
saga-orchestration/
├── services/          # 言語非依存リソース（openapi.yml, Flyway マイグレーション SQL）
├── kotlin/            # Kotlin + Spring Boot 実装
├── go/                # Go 実装
│   └── payment-service/  # 決済管理（Kotlin 版と API 互換）
└── docker/            # Docker Compose, LocalStack, PostgreSQL 初期化
```

- `services/<service>/openapi.yml` — API 定義（言語問わず参照）
- `services/<service>/db/migration/` — Flyway マイグレーション SQL
- Kotlin ビルドは `$rootDir/../services/` 経由でこれらを参照する
  - openapi.yml → OpenAPI Generator の `inputSpec`
  - db/migration/ → `processResources` でビルド成果物にコピー
- Go 実装は同じ DB スキーマを共有し、マイグレーションは Flyway / psql で管理（Go バイナリでは実行しない）

## サービス構成

| サービス | ポート | 役割 | 実装 |
|---------|--------|------|------|
| orchestrator | 8080 | Saga オーケストレーション（同期 / Step Functions 非同期） | Kotlin |
| order-service | 8081 | 注文管理 | Kotlin |
| payment-service | 8083 | 決済管理 | Kotlin / Go（API 互換） |
| compensation-service | 8084 | 補償処理（SQS 経由で非同期実行） | Kotlin |

## DDD（Kotlin）

### Kotlin（Spring Boot）

- 役割ごとにモジュール化する
  - bootstrap, application, domain, infrastructure
- bootstrap
  - Spring Boot の起動点、Controller、設定
  - OpenAPI Generator で openapi.yml から Controller インタフェースを生成し、実装クラスで継承する
- application
  - ユースケースの実装（ApplicationService）
  - ドメインオブジェクトの状態遷移結果を再代入で受け取る（`val completed = order.complete()`）
  - 外部依存は port インタフェースで抽象化し、infrastructure 層で実装する
- domain
  - immutable で実装する
    - フィールドはすべて `val`（`var` + `private set` は使わない）
    - 状態遷移メソッドは新しいインスタンスを返す（`fun complete(): Order`）
    - コレクションは `List`（`MutableList` は使わない）
  - pure Kotlin で実装する（フレームワーク依存なし）
  - ファクトリメソッド: `create()` で新規生成、`reconstitute()` で永続化層から復元
  - 値オブジェクト: `OrderId`, `PaymentId`, `Money` など
  - Repository インタフェースを定義（実装は infrastructure）
- infrastructure
  - JPA Entity とドメインモデルの変換（`toDomain()` / `toEntity()`）
  - 依存ライブラリの影響範囲を分離するため、役割ごとにサブモジュール化する
    - persistence: DB アクセス（Spring Data JPA, Flyway）
    - http: 外部サービスへの HTTP クライアント（WebClient / RestClient）
    - messaging: メッセージング（EventBridge, SQS）
    - aws: AWS サービス連携（Step Functions）

## Go（payment-service）

- DDD レイヤリングを使わず、Go-idiomatic なフラット構成
- `internal/` 配下のパッケージ構成:

```
internal/
  payment/    # ビジネスロジック + HTTP ハンドラ + DTO
  postgres/   # DB 実装（pgx）
  middleware/ # 冪等性、トレース伝播、リクエストログ
  config/     # 環境設定
```

- payment パッケージ
  - `Payment` struct + `Status` 型 + 状態遷移メソッド（`Capture()`, `Refund()`）
  - ファクトリ関数: `NewPayment(orderID, amount)` で新規生成
  - `Store` interface（消費側で定義、Go idiom）
  - `Service`: ユースケース（`Authorize`, `Capture`, `Refund`）
  - `Handler`: `net/http` ハンドラ（Go 1.22+ ServeMux パターンマッチ）
  - `Request` / `Response` DTO
  - エラー型: `FailedError`, `InvalidStatusError`
- postgres パッケージ: `PaymentStore`（pgx）、`IdempotencyStore`
- middleware: 冪等性、トレース伝播、リクエストログ
- DI: 手動コンストラクタ注入（`cmd/server/main.go`）
- テスト: `testing` + `testify`、テスト名は日本語

## 横断関心事（common モジュール）

- 共有 DTO: OpenAPI Generator で common/openapi.yml から生成
- 冪等性: `@Idempotent` アノテーション + `IdempotencyAspect`（`Idempotency-Key` ヘッダで制御）
- 分散トレーシング: `TraceFilter` が `X-Trace-Id` ヘッダを MDC に伝播
- 共通例外: `GlobalExceptionHandler`（`@RestControllerAdvice`）

## 主要パターン

- **Transactional Outbox**: ドメイン操作と外部イベント発行を同一トランザクションで保証
  - orchestrator: `outbox_events` → EventBridge
  - compensation-service: `outbox_tasks` → HTTP 補償呼び出し
- **Saga（同期）**: orchestrator が order → payment → complete を順次呼び出し、失敗時に補償イベントを発行
- **Saga（非同期）**: AWS Step Functions のステートマシンで同等フローを実行

## テスト

- テスト名は日本語で記述する
  - Kotlin: `` fun `新規注文を作成しPENDINGステータスで返す`() ``
  - Go: `func Test_新規決済を作成しAUTHORIZEDステータスで返す(t *testing.T)`

### Kotlin

- 単体テスト: `src/test/kotlin` — MockK でモック
- 結合テスト: `src/integration/kotlin` — `@SpringBootTest` + Flyway + H2
  - 実行: `./gradlew :<service>:application:integrationTest`
- E2E テスト: `kotlin/e2e-test/` — 実サービス群を起動して HTTP 経由で検証
  - 実行: `./gradlew :e2e-test:test`
  - `./gradlew test` では自動スキップされる（`onlyIf` ガード）
  - CI では main push 時のみ実行
- パッケージはテスト対象と同じにする（例: `com.example.order.application`）

### Go

- 単体テスト: 各パッケージの `_test.go` — `testify` でアサーション
  - 実行: `cd go/payment-service && go test ./...`
- CI: `.github/workflows/go-test.yml`（main push / PR の `go/**` パス変更時に実行）
