-- orders
COMMENT ON TABLE orders IS '注文テーブル';
COMMENT ON COLUMN orders.order_id IS '注文ID（主キー）';
COMMENT ON COLUMN orders.product_id IS '商品ID';
COMMENT ON COLUMN orders.quantity IS '数量';
COMMENT ON COLUMN orders.amount IS '金額';
COMMENT ON COLUMN orders.status IS '注文ステータス（PENDING / COMPLETED / CANCELLED）';
COMMENT ON COLUMN orders.created_at IS '作成日時';
COMMENT ON COLUMN orders.updated_at IS '更新日時';

-- idempotency_keys
COMMENT ON TABLE idempotency_keys IS '冪等性キー管理テーブル';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS '冪等性キー（主キー）';
COMMENT ON COLUMN idempotency_keys.response_body IS 'レスポンスボディ';
COMMENT ON COLUMN idempotency_keys.status_code IS 'HTTPステータスコード';
COMMENT ON COLUMN idempotency_keys.created_at IS '作成日時';
