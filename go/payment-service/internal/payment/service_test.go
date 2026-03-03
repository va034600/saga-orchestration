package payment

import (
	"context"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type mockStore struct {
	payments map[string]Payment
}

func newMockStore() *mockStore {
	return &mockStore{payments: make(map[string]Payment)}
}

func (m *mockStore) Save(_ context.Context, p *Payment) error {
	m.payments[p.OrderID] = *p
	return nil
}

func (m *mockStore) FindByOrderID(_ context.Context, orderID string) (*Payment, error) {
	p, ok := m.payments[orderID]
	if !ok {
		return nil, nil
	}
	cp := p
	return &cp, nil
}

func Test_新規決済を承認しAUTHORIZEDステータスで返す(t *testing.T) {
	svc := NewService(newMockStore())
	resp, err := svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})
	require.NoError(t, err)
	assert.Equal(t, "order-1", resp.OrderID)
	assert.Equal(t, "AUTHORIZED", resp.Status)
	assert.True(t, resp.Success)
	assert.NotEmpty(t, resp.PaymentID)
}

func Test_同一注文IDの承認リクエストは既存決済を返す(t *testing.T) {
	svc := NewService(newMockStore())
	resp1, _ := svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})
	resp2, err := svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  2000,
	})
	require.NoError(t, err)
	assert.Equal(t, resp1.PaymentID, resp2.PaymentID)
}

func Test_承認済み決済をキャプチャできる(t *testing.T) {
	svc := NewService(newMockStore())
	svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})

	resp, err := svc.Capture(context.Background(), "order-1")
	require.NoError(t, err)
	assert.Equal(t, "CAPTURED", resp.Status)
	assert.True(t, resp.Success)
}

func Test_存在しない注文のキャプチャはFailedErrorを返す(t *testing.T) {
	svc := NewService(newMockStore())
	_, err := svc.Capture(context.Background(), "nonexistent")
	require.Error(t, err)
	var fe *FailedError
	assert.ErrorAs(t, err, &fe)
}

func Test_承認済み決済をリファンドできる(t *testing.T) {
	svc := NewService(newMockStore())
	svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})

	resp, err := svc.Refund(context.Background(), "order-1")
	require.NoError(t, err)
	assert.Equal(t, "REFUNDED", resp.Status)
}

func Test_存在しない注文のリファンドはFailedErrorを返す(t *testing.T) {
	svc := NewService(newMockStore())
	_, err := svc.Refund(context.Background(), "nonexistent")
	require.Error(t, err)
	var fe *FailedError
	assert.ErrorAs(t, err, &fe)
}

func Test_キャプチャ済み決済をリファンドできる(t *testing.T) {
	svc := NewService(newMockStore())
	svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})
	svc.Capture(context.Background(), "order-1")

	resp, err := svc.Refund(context.Background(), "order-1")
	require.NoError(t, err)
	assert.Equal(t, "REFUNDED", resp.Status)
}

func Test_リファンド済み決済のキャプチャはFailedErrorを返す(t *testing.T) {
	svc := NewService(newMockStore())
	svc.Authorize(context.Background(), Request{
		OrderID: "order-1",
		Amount:  1000,
	})
	svc.Refund(context.Background(), "order-1")

	_, err := svc.Capture(context.Background(), "order-1")
	require.Error(t, err)
	var fe *FailedError
	assert.ErrorAs(t, err, &fe)
}
