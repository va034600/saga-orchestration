package payment

import (
	"fmt"
	"time"

	"github.com/google/uuid"
)

type Status string

const (
	StatusAuthorized Status = "AUTHORIZED"
	StatusCaptured   Status = "CAPTURED"
	StatusRefunded   Status = "REFUNDED"
	StatusFailed     Status = "FAILED"
)

var validTransitions = map[Status][]Status{
	StatusAuthorized: {StatusCaptured, StatusRefunded},
	StatusCaptured:   {StatusRefunded},
	StatusRefunded:   {},
	StatusFailed:     {},
}

func (s Status) CanTransitionTo(target Status) bool {
	for _, t := range validTransitions[s] {
		if t == target {
			return true
		}
	}
	return false
}

type Payment struct {
	ID        string
	OrderID   string
	Amount    int64
	Status    Status
	CreatedAt time.Time
	UpdatedAt time.Time
}

func NewPayment(orderID string, amount int64) (*Payment, error) {
	if amount < 0 {
		return nil, fmt.Errorf("amount must be >= 0, got %d", amount)
	}
	now := time.Now().UTC()
	return &Payment{
		ID:        uuid.New().String(),
		OrderID:   orderID,
		Amount:    amount,
		Status:    StatusAuthorized,
		CreatedAt: now,
		UpdatedAt: now,
	}, nil
}

func (p *Payment) Capture() error {
	if !p.Status.CanTransitionTo(StatusCaptured) {
		return &InvalidStatusError{
			Current: p.Status,
			Target:  StatusCaptured,
		}
	}
	p.Status = StatusCaptured
	p.UpdatedAt = time.Now().UTC()
	return nil
}

func (p *Payment) Refund() error {
	if !p.Status.CanTransitionTo(StatusRefunded) {
		return &InvalidStatusError{
			Current: p.Status,
			Target:  StatusRefunded,
		}
	}
	p.Status = StatusRefunded
	p.UpdatedAt = time.Now().UTC()
	return nil
}

type FailedError struct {
	OrderID string
	Reason  string
}

func (e *FailedError) Error() string {
	return fmt.Sprintf("Payment failed for order %s: %s", e.OrderID, e.Reason)
}

type InvalidStatusError struct {
	Current Status
	Target  Status
}

func (e *InvalidStatusError) Error() string {
	return fmt.Sprintf("Cannot transition from %s to %s", e.Current, e.Target)
}
