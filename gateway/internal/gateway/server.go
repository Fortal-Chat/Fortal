package gateway

import (
	"log/slog"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

type Server struct {
	heartbeatInterval time.Duration
	upgrader          websocket.Upgrader
}

func NewServer(heartbeatInterval time.Duration) *Server {
	return &Server{heartbeatInterval: heartbeatInterval, upgrader: websocket.Upgrader{}}
}

func (s *Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	conn, err := s.upgrader.Upgrade(w, r, nil)
	if err != nil {
		slog.Warn("upgrade failed", "remote", r.RemoteAddr, "err", err)
		return
	}

	session := newSession(conn, s.heartbeatInterval)
	session.run()
}
