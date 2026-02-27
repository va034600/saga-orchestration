CREATE TABLE payments (
    payment_id  VARCHAR(64)    PRIMARY KEY,
    order_id    VARCHAR(64)    NOT NULL,
    amount      NUMERIC(19,2)  NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'AUTHORIZED',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
