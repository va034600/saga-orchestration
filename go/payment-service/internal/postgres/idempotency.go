package postgres

import (
	"context"

	"github.com/example/payment-service/internal/middleware"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type IdempotencyStore struct {
	pool *pgxpool.Pool
}

func NewIdempotencyStore(pool *pgxpool.Pool) *IdempotencyStore {
	return &IdempotencyStore{pool: pool}
}

func (s *IdempotencyStore) Find(ctx context.Context, key string) (*middleware.IdempotencyRecord, error) {
	var rec middleware.IdempotencyRecord
	err := s.pool.QueryRow(ctx, `
		SELECT idempotency_key, status_code, response_body
		FROM idempotency_keys WHERE idempotency_key = $1
	`, key).Scan(&rec.Key, &rec.StatusCode, &rec.Body)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	return &rec, nil
}

func (s *IdempotencyStore) Insert(ctx context.Context, key string) error {
	_, err := s.pool.Exec(ctx, `
		INSERT INTO idempotency_keys (idempotency_key) VALUES ($1)
	`, key)
	return err
}

func (s *IdempotencyStore) Update(ctx context.Context, key string, statusCode int, body string) error {
	_, err := s.pool.Exec(ctx, `
		UPDATE idempotency_keys SET status_code = $1, response_body = $2 WHERE idempotency_key = $3
	`, statusCode, body, key)
	return err
}
