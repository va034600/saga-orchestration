- このサービスはマイクロサービスで設計する
- 各アプリはDDDで実装する
- 言語: Kotlin + Spring Boot
- ビルド: `./gradlew clean build`（kotlin/ ディレクトリで実行）

## ディレクトリ構成

```
saga-orchestration/
├── services/          # 言語非依存リソース（openapi.yml, Flyway マイグレーション SQL）
├── kotlin/            # Kotlin + Spring Boot 実装
└── docker/            # Docker Compose, LocalStack, PostgreSQL 初期化
```

- `services/<service>/openapi.yml` — API 定義（言語問わず参照）
- `services/<service>/db/migration/` — Flyway マイグレーション SQL
- Kotlin ビルドは `$rootDir/../services/` 経由でこれらを参照する
  - openapi.yml → OpenAPI Generator の `inputSpec`
  - db/migration/ → `processResources` でビルド成果物にコピー

## サービス構成

| サービス | ポート | 役割 |
|---------|--------|------|
| orchestrator | 8080 | Saga オーケストレーション（同期 / Step Functions 非同期） |
| order-service | 8081 | 注文管理 |
| payment-service | 8083 | 決済管理 |
| compensation-service | 8084 | 補償処理（SQS 経由で非同期実行） |

## DDD

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

- テスト名は日本語で記述する（`` fun `新規注文を作成しPENDINGステータスで返す`() ``）
- 単体テスト: `src/test/kotlin` — MockK でモック
- 結合テスト: `src/integration/kotlin` — `@SpringBootTest` + Flyway + H2
  - 実行: `./gradlew :<service>:application:integrationTest`
- パッケージはテスト対象と同じにする（例: `com.example.order.application`）
