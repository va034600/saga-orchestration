package postgres

import (
	"context"
	"time"

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
		p         payment.Payment
		status    string
		createdAt time.Time
		updatedAt time.Time
	)
	err := s.pool.QueryRow(ctx, `
		SELECT payment_id, order_id, amount, status, created_at, updated_at
		FROM payments WHERE order_id = $1
	`, orderID).Scan(&p.ID, &p.OrderID, &p.Amount, &status, &createdAt, &updatedAt)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	p.Status = payment.Status(status)
	p.CreatedAt = createdAt
	p.UpdatedAt = updatedAt
	return &p, nil
}
