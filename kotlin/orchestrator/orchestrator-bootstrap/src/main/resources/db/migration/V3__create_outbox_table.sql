CREATE TABLE outbox_events (
    id            BIGSERIAL      PRIMARY KEY,
    order_id      VARCHAR(64)    NOT NULL,
    event_type    VARCHAR(50)    NOT NULL,
    payload       TEXT           NOT NULL,
    published     BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    published_at  TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_unpublished ON outbox_events (published, created_at)
    WHERE published = FALSE;
