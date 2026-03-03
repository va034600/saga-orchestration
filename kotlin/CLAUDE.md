- DDD で実装する
- 言語: Kotlin + Spring Boot
- ビルド: `./gradlew clean build`
- Kotlin ビルドは `$rootDir/../services/` 経由で言語非依存リソースを参照する
  - openapi.yml → OpenAPI Generator の `inputSpec`
  - db/migration/ → `processResources` でビルド成果物にコピー

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

## テスト

- テスト名は日本語で記述する（`` fun `新規注文を作成しPENDINGステータスで返す`() ``）
- 単体テスト: `src/test/kotlin` — MockK でモック
- 結合テスト: `src/integration/kotlin` — `@SpringBootTest` + Flyway + H2
  - 実行: `./gradlew :<service>:application:integrationTest`
- E2E テスト: `kotlin/e2e-test/` — 実サービス群を起動して HTTP 経由で検証
  - 実行: `./gradlew :e2e-test:test`
  - `./gradlew test` では自動スキップされる（`onlyIf` ガード）
  - CI では main push 時のみ実行
- パッケージはテスト対象と同じにする（例: `com.example.order.application`）
