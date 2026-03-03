package payment

import (
	"encoding/json"
	"errors"
	"net/http"
	"time"
)

type Request struct {
	OrderID string `json:"orderId"`
	Amount  int64  `json:"amount"`
}

type Response struct {
	PaymentID string `json:"paymentId"`
	OrderID   string `json:"orderId"`
	Status    string `json:"status"`
	Success   bool   `json:"success"`
}

type errorResponse struct {
	Timestamp string `json:"timestamp"`
	Status    int    `json:"status"`
	Error     string `json:"error"`
	Message   string `json:"message"`
}

type Handler struct {
	svc *Service
}

func NewHandler(svc *Service) *Handler {
	return &Handler{svc: svc}
}

func (h *Handler) Authorize(w http.ResponseWriter, r *http.Request) {
	var req Request
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "Bad Request", "Invalid request body")
		return
	}

	resp, err := h.svc.Authorize(r.Context(), req)
	if err != nil {
		handleError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func (h *Handler) Capture(w http.ResponseWriter, r *http.Request) {
	orderID := r.PathValue("orderId")
	if orderID == "" {
		writeError(w, http.StatusBadRequest, "Bad Request", "orderId is required")
		return
	}

	resp, err := h.svc.Capture(r.Context(), orderID)
	if err != nil {
		handleError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func (h *Handler) Refund(w http.ResponseWriter, r *http.Request) {
	orderID := r.PathValue("orderId")
	if orderID == "" {
		writeError(w, http.StatusBadRequest, "Bad Request", "orderId is required")
		return
	}

	resp, err := h.svc.Refund(r.Context(), orderID)
	if err != nil {
		handleError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

func HealthHandler() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"status": "UP"})
	}
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, errText string, message string) {
	resp := errorResponse{
		Timestamp: time.Now().UTC().Format(time.RFC3339Nano),
		Status:    status,
		Error:     errText,
		Message:   message,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(resp)
}

func handleError(w http.ResponseWriter, err error) {
	var failed *FailedError
	var stateErr *InvalidStatusError

	switch {
	case errors.As(err, &failed):
		writeError(w, http.StatusUnprocessableEntity, "Unprocessable Entity", err.Error())
	case errors.As(err, &stateErr):
		writeError(w, http.StatusUnprocessableEntity, "Unprocessable Entity", err.Error())
	default:
		writeError(w, http.StatusInternalServerError, "Internal Server Error", err.Error())
	}
}
