CREATE TABLE outbox_tasks (
    id                 BIGSERIAL      PRIMARY KEY,
    compensation_id    BIGINT         NOT NULL REFERENCES compensations(comp_id),
    order_id           VARCHAR(64)    NOT NULL,
    task_type          VARCHAR(50)    NOT NULL,
    payload            TEXT           NOT NULL,
    published          BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    published_at       TIMESTAMPTZ
);

CREATE INDEX idx_outbox_tasks_unpublished ON outbox_tasks (published, created_at)
    WHERE published = FALSE;
