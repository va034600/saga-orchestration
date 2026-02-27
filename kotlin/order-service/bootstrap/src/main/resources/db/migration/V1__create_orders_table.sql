CREATE TABLE orders (
    order_id    VARCHAR(64)    PRIMARY KEY,
    product_id  VARCHAR(64)    NOT NULL,
    quantity    INTEGER        NOT NULL,
    amount      NUMERIC(19,2)  NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);
