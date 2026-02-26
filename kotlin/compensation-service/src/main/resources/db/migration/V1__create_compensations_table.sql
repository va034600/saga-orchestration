CREATE TABLE compensations (
    comp_id            BIGSERIAL      PRIMARY KEY,
    order_id           VARCHAR(64)    NOT NULL,
    compensation_type  VARCHAR(30)    NOT NULL,
    status             VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    error_message      TEXT,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    completed_at       TIMESTAMPTZ
);

CREATE INDEX idx_compensations_order_id ON compensations (order_id);
