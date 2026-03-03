package payment

import (
	"context"
	"errors"
)

type Store interface {
	Save(ctx context.Context, payment *Payment) error
	FindByOrderID(ctx context.Context, orderID string) (*Payment, error)
}

type Service struct {
	store Store
}

func NewService(store Store) *Service {
	return &Service{store: store}
}

func (s *Service) Authorize(ctx context.Context, req Request) (Response, error) {
	existing, err := s.store.FindByOrderID(ctx, req.OrderID)
	if err != nil {
		return Response{}, err
	}
	if existing != nil {
		return toResponse(existing), nil
	}

	p, err := NewPayment(req.OrderID, req.Amount)
	if err != nil {
		return Response{}, err
	}

	if err := s.store.Save(ctx, p); err != nil {
		return Response{}, err
	}

	return toResponse(p), nil
}

func (s *Service) Capture(ctx context.Context, orderID string) (Response, error) {
	p, err := s.store.FindByOrderID(ctx, orderID)
	if err != nil {
		return Response{}, err
	}
	if p == nil {
		return Response{}, &FailedError{
			OrderID: orderID,
			Reason:  "No authorized payment found",
		}
	}

	if err := p.Capture(); err != nil {
		var stateErr *InvalidStatusError
		if errors.As(err, &stateErr) {
			return Response{}, &FailedError{
				OrderID: orderID,
				Reason:  "Payment is not in AUTHORIZED status: " + string(p.Status),
			}
		}
		return Response{}, err
	}

	if err := s.store.Save(ctx, p); err != nil {
		return Response{}, err
	}

	return toResponse(p), nil
}

func (s *Service) Refund(ctx context.Context, orderID string) (Response, error) {
	p, err := s.store.FindByOrderID(ctx, orderID)
	if err != nil {
		return Response{}, err
	}
	if p == nil {
		return Response{}, &FailedError{
			OrderID: orderID,
			Reason:  "No payment found to refund",
		}
	}

	if err := p.Refund(); err != nil {
		return Response{}, err
	}

	if err := s.store.Save(ctx, p); err != nil {
		return Response{}, err
	}

	return toResponse(p), nil
}

func toResponse(p *Payment) Response {
	return Response{
		PaymentID: p.ID,
		OrderID:   p.OrderID,
		Status:    string(p.Status),
		Success:   p.Status != StatusFailed,
	}
}
