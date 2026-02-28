-- saga_states
COMMENT ON TABLE saga_states IS 'Saga 実行状態を管理するテーブル';
COMMENT ON COLUMN saga_states.order_id IS '注文ID（主キー）';
COMMENT ON COLUMN saga_states.status IS 'Saga の状態（STARTED / COMPLETED / FAILED）';
COMMENT ON COLUMN saga_states.current_step IS '現在実行中のステップ名';
COMMENT ON COLUMN saga_states.created_at IS '作成日時';
COMMENT ON COLUMN saga_states.updated_at IS '更新日時';

-- saga_steps
COMMENT ON TABLE saga_steps IS 'Saga の各ステップの実行履歴';
COMMENT ON COLUMN saga_steps.id IS 'ステップID（自動採番）';
COMMENT ON COLUMN saga_steps.order_id IS '注文ID（saga_states への外部キー）';
COMMENT ON COLUMN saga_steps.step_name IS 'ステップ名';
COMMENT ON COLUMN saga_steps.status IS 'ステップの状態（PENDING / SUCCESS / FAILED）';
COMMENT ON COLUMN saga_steps.error_message IS 'エラーメッセージ';
COMMENT ON COLUMN saga_steps.executed_at IS '実行日時';

-- outbox_events
COMMENT ON TABLE outbox_events IS 'Transactional Outbox パターン用のイベントテーブル';
COMMENT ON COLUMN outbox_events.id IS 'イベントID（自動採番）';
COMMENT ON COLUMN outbox_events.order_id IS '注文ID';
COMMENT ON COLUMN outbox_events.event_type IS 'イベント種別';
COMMENT ON COLUMN outbox_events.payload IS 'イベントペイロード（JSON）';
COMMENT ON COLUMN outbox_events.published IS '発行済みフラグ';
COMMENT ON COLUMN outbox_events.created_at IS '作成日時';
COMMENT ON COLUMN outbox_events.published_at IS '発行日時';

-- idempotency_keys
COMMENT ON TABLE idempotency_keys IS '冪等性キー管理テーブル';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS '冪等性キー（主キー）';
COMMENT ON COLUMN idempotency_keys.response_body IS 'レスポンスボディ';
COMMENT ON COLUMN idempotency_keys.status_code IS 'HTTPステータスコード';
COMMENT ON COLUMN idempotency_keys.created_at IS '作成日時';
