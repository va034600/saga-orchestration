-- payments
COMMENT ON TABLE payments IS '決済テーブル';
COMMENT ON COLUMN payments.payment_id IS '決済ID（主キー）';
COMMENT ON COLUMN payments.order_id IS '注文ID';
COMMENT ON COLUMN payments.amount IS '金額';
COMMENT ON COLUMN payments.status IS '決済ステータス（AUTHORIZED / CAPTURED / REFUNDED）';
COMMENT ON COLUMN payments.created_at IS '作成日時';
COMMENT ON COLUMN payments.updated_at IS '更新日時';

-- idempotency_keys
COMMENT ON TABLE idempotency_keys IS '冪等性キー管理テーブル';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS '冪等性キー（主キー）';
COMMENT ON COLUMN idempotency_keys.response_body IS 'レスポンスボディ';
COMMENT ON COLUMN idempotency_keys.status_code IS 'HTTPステータスコード';
COMMENT ON COLUMN idempotency_keys.created_at IS '作成日時';
