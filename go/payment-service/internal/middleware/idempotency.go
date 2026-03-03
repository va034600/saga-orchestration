package middleware

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"time"
)

type IdempotencyStore interface {
	Find(ctx context.Context, key string) (*IdempotencyRecord, error)
	Insert(ctx context.Context, key string) error
	Update(ctx context.Context, key string, statusCode int, body string) error
}

type IdempotencyRecord struct {
	Key        string
	StatusCode *int
	Body       *string
}

type captureWriter struct {
	http.ResponseWriter
	status int
	body   bytes.Buffer
}

func (w *captureWriter) WriteHeader(code int) {
	w.status = code
	w.ResponseWriter.WriteHeader(code)
}

func (w *captureWriter) Write(b []byte) (int, error) {
	w.body.Write(b)
	return w.ResponseWriter.Write(b)
}

func Idempotency(store IdempotencyStore) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			key := r.Header.Get("Idempotency-Key")
			if key == "" {
				next.ServeHTTP(w, r)
				return
			}

			ctx := r.Context()

			rec, err := store.Find(ctx, key)
			if err != nil {
				slog.Error("idempotency store lookup failed", "error", err)
				next.ServeHTTP(w, r)
				return
			}

			if rec != nil {
				if rec.Body != nil && rec.StatusCode != nil {
					w.Header().Set("Content-Type", "application/json")
					w.WriteHeader(*rec.StatusCode)
					w.Write([]byte(*rec.Body))
					return
				}
				handleDuplicateRequest(w, key)
				return
			}

			if err := store.Insert(ctx, key); err != nil {
				slog.Error("idempotency store insert failed", "error", err)
				next.ServeHTTP(w, r)
				return
			}

			cw := &captureWriter{ResponseWriter: w, status: http.StatusOK}
			next.ServeHTTP(cw, r)

			if err := store.Update(ctx, key, cw.status, cw.body.String()); err != nil {
				slog.Error("idempotency store update failed", "error", err)
			}
		})
	}
}

func handleDuplicateRequest(w http.ResponseWriter, key string) {
	resp := map[string]any{
		"timestamp": time.Now().UTC().Format(time.RFC3339Nano),
		"status":    http.StatusConflict,
		"error":     "Conflict",
		"message":   fmt.Sprintf("Duplicate request: %s", key),
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusConflict)
	json.NewEncoder(w).Encode(resp)
}
