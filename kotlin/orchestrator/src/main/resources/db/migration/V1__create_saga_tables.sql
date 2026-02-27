CREATE TABLE saga_states (
    order_id     VARCHAR(64)    PRIMARY KEY,
    status       VARCHAR(20)    NOT NULL DEFAULT 'STARTED',
    current_step VARCHAR(50),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE saga_steps (
    id            BIGSERIAL      PRIMARY KEY,
    order_id      VARCHAR(64)    NOT NULL REFERENCES saga_states(order_id),
    step_name     VARCHAR(50)    NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    executed_at   TIMESTAMPTZ
);

CREATE INDEX idx_saga_steps_order_id ON saga_steps (order_id);
