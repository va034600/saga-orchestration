package payment

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func newTestHandler() *Handler {
	return NewHandler(NewService(newMockStore()))
}

func Test_Authorize_正常リクエストで200を返す(t *testing.T) {
	h := newTestHandler()
	body, _ := json.Marshal(Request{OrderID: "order-1", Amount: 1000})
	req := httptest.NewRequest(http.MethodPost, "/api/payments/authorize", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	h.Authorize(rec, req)

	assert.Equal(t, http.StatusOK, rec.Code)
	var resp Response
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &resp))
	assert.Equal(t, "order-1", resp.OrderID)
	assert.Equal(t, "AUTHORIZED", resp.Status)
	assert.True(t, resp.Success)
}

func Test_Authorize_不正なJSONで400を返す(t *testing.T) {
	h := newTestHandler()
	req := httptest.NewRequest(http.MethodPost, "/api/payments/authorize", bytes.NewReader([]byte("invalid")))
	rec := httptest.NewRecorder()

	h.Authorize(rec, req)

	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func Test_Capture_存在しない注文で422を返す(t *testing.T) {
	h := newTestHandler()
	req := httptest.NewRequest(http.MethodPut, "/api/payments/nonexistent/capture", nil)
	req.SetPathValue("orderId", "nonexistent")
	rec := httptest.NewRecorder()

	h.Capture(rec, req)

	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
}

func Test_Capture_承認済み注文で200を返す(t *testing.T) {
	h := newTestHandler()

	body, _ := json.Marshal(Request{OrderID: "order-1", Amount: 500})
	authReq := httptest.NewRequest(http.MethodPost, "/api/payments/authorize", bytes.NewReader(body))
	authReq.Header.Set("Content-Type", "application/json")
	h.Authorize(httptest.NewRecorder(), authReq)

	capReq := httptest.NewRequest(http.MethodPut, "/api/payments/order-1/capture", nil)
	capReq.SetPathValue("orderId", "order-1")
	rec := httptest.NewRecorder()
	h.Capture(rec, capReq)

	assert.Equal(t, http.StatusOK, rec.Code)
	var resp Response
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &resp))
	assert.Equal(t, "CAPTURED", resp.Status)
}

func Test_Refund_存在しない注文で422を返す(t *testing.T) {
	h := newTestHandler()
	req := httptest.NewRequest(http.MethodPut, "/api/payments/nonexistent/refund", nil)
	req.SetPathValue("orderId", "nonexistent")
	rec := httptest.NewRecorder()

	h.Refund(rec, req)

	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
}

func Test_Refund_承認済み注文で200を返す(t *testing.T) {
	h := newTestHandler()

	body, _ := json.Marshal(Request{OrderID: "order-1", Amount: 500})
	authReq := httptest.NewRequest(http.MethodPost, "/api/payments/authorize", bytes.NewReader(body))
	authReq.Header.Set("Content-Type", "application/json")
	h.Authorize(httptest.NewRecorder(), authReq)

	refReq := httptest.NewRequest(http.MethodPut, "/api/payments/order-1/refund", nil)
	refReq.SetPathValue("orderId", "order-1")
	rec := httptest.NewRecorder()
	h.Refund(rec, refReq)

	assert.Equal(t, http.StatusOK, rec.Code)
	var resp Response
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &resp))
	assert.Equal(t, "REFUNDED", resp.Status)
}
