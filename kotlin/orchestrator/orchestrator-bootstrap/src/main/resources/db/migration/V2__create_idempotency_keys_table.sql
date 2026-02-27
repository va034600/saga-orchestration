CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255)   PRIMARY KEY,
    response_body   TEXT,
    status_code     INTEGER,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);
