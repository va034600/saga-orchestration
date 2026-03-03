package postgres

import (
	"context"
	"errors"

	"github.com/example/payment-service/internal/payment"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type PaymentStore struct {
	pool *pgxpool.Pool
}

func NewPaymentStore(pool *pgxpool.Pool) *PaymentStore {
	return &PaymentStore{pool: pool}
}

func (s *PaymentStore) Save(ctx context.Context, p *payment.Payment) error {
	_, err := s.pool.Exec(ctx, `
		INSERT INTO payments (payment_id, order_id, amount, status, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6)
		ON CONFLICT (payment_id) DO UPDATE
		SET status = EXCLUDED.status, updated_at = EXCLUDED.updated_at
	`, p.ID, p.OrderID, p.Amount, string(p.Status), p.CreatedAt, p.UpdatedAt)
	return err
}

func (s *PaymentStore) FindByOrderID(ctx context.Context, orderID string) (*payment.Payment, error) {
	var (
		p      payment.Payment
		status string
	)
	err := s.pool.QueryRow(ctx, `
		SELECT payment_id, order_id, amount, status, created_at, updated_at
		FROM payments WHERE order_id = $1
	`, orderID).Scan(&p.ID, &p.OrderID, &p.Amount, &status, &p.CreatedAt, &p.UpdatedAt)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	p.Status = payment.Status(status)
	return &p, nil
}
