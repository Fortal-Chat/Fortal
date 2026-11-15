package gateway

import (
	"encoding/json"
	"log/slog"
	"time"

	"github.com/Fortal-Chat/Fortal/gateway/internal/protocol"
	"github.com/gorilla/websocket"
)

type Session struct {
	conn              *websocket.Conn
	heartbeatInterval time.Duration
	log               *slog.Logger
}

func newSession(conn *websocket.Conn, heartbeatInterval time.Duration) *Session {
	return &Session{
		conn:              conn,
		heartbeatInterval: heartbeatInterval,
		log:               slog.With("remote", conn.RemoteAddr().String()),
	}
}

func (s *Session) run() {
	defer s.conn.Close()
	s.log.Info("client connected")

	hello := protocol.HelloData{HeartbeatInterval: int(s.heartbeatInterval.Milliseconds())}
	if err := s.send(protocol.OpHello, hello); err != nil {
		s.log.Warn("send hello failed", "err", err)
		return
	}

	for {
		err := s.conn.SetReadDeadline(time.Now().Add(s.heartbeatInterval * 3))
		if err != nil {
			s.log.Debug("set read deadline failed", "err", err)
			return
		}
		msgType, data, err := s.conn.ReadMessage()
		if err != nil {
			s.log.Info("client disconnected", "err", err)
			return
		}
		if msgType != websocket.TextMessage {
			continue
		}
		if err := s.handle(data); err != nil {
			s.log.Warn("handle message failed", "err", err)
			return
		}
	}
}

func (s *Session) handle(data []byte) error {
	var msg protocol.Message
	if err := json.Unmarshal(data, &msg); err != nil {
		s.log.Debug("unmarshal failed", "err", err)
		return s.sendError(protocol.ErrorData{Code: protocol.ErrBadJSON, Message: "invalid JSON"})
	}

	if msg.Op == "" {
		return s.sendError(protocol.ErrorData{Code: protocol.ErrNoOp, Message: "no operation"})
	}

	switch msg.Op {
	case protocol.OpEcho:
		return s.sendRaw(protocol.Message{Op: protocol.OpEcho, D: msg.D})

	case protocol.OpPing:
		return s.send(protocol.OpPong, nil)

	default:
		s.log.Debug("unknown op", "op", msg.Op)
		return s.sendError(protocol.ErrorData{Code: protocol.ErrUnknownOp, Message: "unknown operation" + msg.Op})
	}
}

func (s *Session) send(op string, d any) error {
	msg, err := protocol.New(op, d)
	if err != nil {
		return err
	}
	return s.sendRaw(msg)
}

func (s *Session) sendError(errorData protocol.ErrorData) error {
	return s.send(protocol.OpError, errorData)
}

func (s *Session) sendRaw(msg protocol.Message) error {
	return s.conn.WriteJSON(msg)
}
