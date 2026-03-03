- DDD レイヤリングを使わず、Go-idiomatic なフラット構成
- ビルド: `go build ./...`（go/payment-service/ ディレクトリで実行）
- DB スキーマは Kotlin 版と共有し、マイグレーションは Flyway / psql で管理（Go バイナリでは実行しない）

## パッケージ構成

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

## テスト

- テスト名は日本語で記述する（`func Test_新規決済を作成しAUTHORIZEDステータスで返す(t *testing.T)`）
- 単体テスト: 各パッケージの `_test.go` — `testify` でアサーション
  - 実行: `go test ./...`
- CI: `.github/workflows/go-test.yml`（main push / PR の `go/**` パス変更時に実行）
