-- compensations
COMMENT ON TABLE compensations IS '補償処理テーブル';
COMMENT ON COLUMN compensations.comp_id IS '補償ID（自動採番）';
COMMENT ON COLUMN compensations.order_id IS '注文ID';
COMMENT ON COLUMN compensations.compensation_type IS '補償種別（CANCEL_ORDER / REFUND_PAYMENT）';
COMMENT ON COLUMN compensations.status IS '補償ステータス（PENDING / COMPLETED / FAILED）';
COMMENT ON COLUMN compensations.error_message IS 'エラーメッセージ';
COMMENT ON COLUMN compensations.created_at IS '作成日時';
COMMENT ON COLUMN compensations.completed_at IS '完了日時';

-- outbox_tasks
COMMENT ON TABLE outbox_tasks IS 'Transactional Outbox パターン用の補償タスクテーブル';
COMMENT ON COLUMN outbox_tasks.id IS 'タスクID（自動採番）';
COMMENT ON COLUMN outbox_tasks.compensation_id IS '補償ID（compensations への外部キー）';
COMMENT ON COLUMN outbox_tasks.order_id IS '注文ID';
COMMENT ON COLUMN outbox_tasks.task_type IS 'タスク種別';
COMMENT ON COLUMN outbox_tasks.payload IS 'タスクペイロード（JSON）';
COMMENT ON COLUMN outbox_tasks.published IS '実行済みフラグ';
COMMENT ON COLUMN outbox_tasks.created_at IS '作成日時';
COMMENT ON COLUMN outbox_tasks.published_at IS '実行日時';

-- idempotency_keys
COMMENT ON TABLE idempotency_keys IS '冪等性キー管理テーブル';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS '冪等性キー（主キー）';
COMMENT ON COLUMN idempotency_keys.response_body IS 'レスポンスボディ';
COMMENT ON COLUMN idempotency_keys.status_code IS 'HTTPステータスコード';
COMMENT ON COLUMN idempotency_keys.created_at IS '作成日時';
