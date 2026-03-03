package main

import (
	"context"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/example/payment-service/internal/config"
	"github.com/example/payment-service/internal/middleware"
	"github.com/example/payment-service/internal/payment"
	"github.com/example/payment-service/internal/postgres"
	"github.com/jackc/pgx/v5/pgxpool"
)

func main() {
	cfg := config.Load()

	slog.SetDefault(slog.New(slog.NewJSONHandler(os.Stdout, nil)))

	pool, err := pgxpool.New(context.Background(), cfg.DatabaseURL)
	if err != nil {
		slog.Error("failed to connect to database", "error", err)
		os.Exit(1)
	}
	defer pool.Close()

	if err := pool.Ping(context.Background()); err != nil {
		slog.Error("failed to ping database", "error", err)
		os.Exit(1)
	}

	store := postgres.NewPaymentStore(pool)
	idempotencyStore := postgres.NewIdempotencyStore(pool)
	svc := payment.NewService(store)
	h := payment.NewHandler(svc)

	mux := http.NewServeMux()

	idempotent := middleware.Idempotency(idempotencyStore)

	mux.Handle("POST /api/payments/authorize", idempotent(http.HandlerFunc(h.Authorize)))
	mux.Handle("PUT /api/payments/{orderId}/capture", idempotent(http.HandlerFunc(h.Capture)))
	mux.Handle("PUT /api/payments/{orderId}/refund", idempotent(http.HandlerFunc(h.Refund)))
	mux.HandleFunc("GET /actuator/health", payment.HealthHandler())

	var root http.Handler = mux
	root = middleware.Logging(root)
	root = middleware.Trace(root)

	srv := &http.Server{
		Addr:    cfg.Addr(),
		Handler: root,
	}

	go func() {
		slog.Info("starting server", "addr", cfg.Addr())
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			slog.Error("server error", "error", err)
			os.Exit(1)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	slog.Info("shutting down server")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		slog.Error("server shutdown error", "error", err)
	}
}
