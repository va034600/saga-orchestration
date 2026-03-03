package payment

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func Test_新規決済を作成しAUTHORIZEDステータスで返す(t *testing.T) {
	p, err := NewPayment("order-1", 1000)
	require.NoError(t, err)

	assert.Equal(t, "order-1", p.OrderID)
	assert.Equal(t, StatusAuthorized, p.Status)
	assert.Equal(t, int64(1000), p.Amount)
}

func Test_負の金額はエラーを返す(t *testing.T) {
	_, err := NewPayment("order-1", -1)
	require.Error(t, err)
}

func Test_AUTHORIZED決済をキャプチャできる(t *testing.T) {
	p, _ := NewPayment("order-1", 500)

	err := p.Capture()
	require.NoError(t, err)
	assert.Equal(t, StatusCaptured, p.Status)
}

func Test_AUTHORIZED決済をリファンドできる(t *testing.T) {
	p, _ := NewPayment("order-1", 500)

	err := p.Refund()
	require.NoError(t, err)
	assert.Equal(t, StatusRefunded, p.Status)
}

func Test_CAPTURED決済をリファンドできる(t *testing.T) {
	p, _ := NewPayment("order-1", 500)
	p.Capture()

	err := p.Refund()
	require.NoError(t, err)
	assert.Equal(t, StatusRefunded, p.Status)
}

func Test_CAPTURED決済を再キャプチャできない(t *testing.T) {
	p, _ := NewPayment("order-1", 500)
	p.Capture()

	err := p.Capture()
	require.Error(t, err)
	var stateErr *InvalidStatusError
	assert.ErrorAs(t, err, &stateErr)
}

func Test_REFUNDED決済はキャプチャできない(t *testing.T) {
	p, _ := NewPayment("order-1", 500)
	p.Refund()

	err := p.Capture()
	require.Error(t, err)
}

func Test_AUTHORIZEDからCAPTUREDに遷移できる(t *testing.T) {
	assert.True(t, StatusAuthorized.CanTransitionTo(StatusCaptured))
}

func Test_AUTHORIZEDからREFUNDEDに遷移できる(t *testing.T) {
	assert.True(t, StatusAuthorized.CanTransitionTo(StatusRefunded))
}

func Test_CAPTUREDからREFUNDEDに遷移できる(t *testing.T) {
	assert.True(t, StatusCaptured.CanTransitionTo(StatusRefunded))
}

func Test_CAPTUREDからAUTHORIZEDには遷移できない(t *testing.T) {
	assert.False(t, StatusCaptured.CanTransitionTo(StatusAuthorized))
}

func Test_REFUNDEDからは遷移できない(t *testing.T) {
	assert.False(t, StatusRefunded.CanTransitionTo(StatusAuthorized))
	assert.False(t, StatusRefunded.CanTransitionTo(StatusCaptured))
}

func Test_FAILEDからは遷移できない(t *testing.T) {
	assert.False(t, StatusFailed.CanTransitionTo(StatusAuthorized))
	assert.False(t, StatusFailed.CanTransitionTo(StatusCaptured))
	assert.False(t, StatusFailed.CanTransitionTo(StatusRefunded))
}
