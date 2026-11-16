package main

import (
	"log/slog"
	"net/http"
	"os"
	"time"

	"github.com/Fortal-Chat/Fortal/gateway/internal/gateway"
)

func main() {
	addr := getenv("GATEWAY_ADDR", ":8081")
	heartbeat := 30 * time.Second

	gw := gateway.NewServer(heartbeat)

	http.Handle("/", gw)

	slog.Info("gateway listening", "addr", addr)
	if err := http.ListenAndServe(addr, nil); err != nil {
		slog.Error("server stopped", "err", err)
		os.Exit(1)
	}
}

func getenv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
