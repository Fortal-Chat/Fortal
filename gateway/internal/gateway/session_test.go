package gateway

import (
	"encoding/json"
	"errors"
	"net"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Fortal-Chat/Fortal/gateway/internal/protocol"
	"github.com/gorilla/websocket"
)

const normalInterval = 30 * time.Second

const fastInterval = 50 * time.Millisecond

func connect(t *testing.T, interval time.Duration) *websocket.Conn {
	t.Helper()

	srv := httptest.NewServer(NewServer(interval))
	t.Cleanup(srv.Close)

	url := "ws" + strings.TrimPrefix(srv.URL, "http")

	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		t.Fatalf("dial %s: %v", url, err)
	}
	t.Cleanup(func() { conn.Close() })
	return conn
}

func connectReady(t *testing.T, interval time.Duration) *websocket.Conn {
	t.Helper()
	conn := connect(t, interval)
	if msg := readMessage(t, conn); msg.Op != protocol.OpHello {
		t.Fatalf("first message op = %q, want %q", msg.Op, protocol.OpHello)
	}
	return conn
}

func readMessage(t *testing.T, conn *websocket.Conn) protocol.Message {
	t.Helper()
	conn.SetReadDeadline(time.Now().Add(2 * time.Second))

	var msg protocol.Message
	if err := conn.ReadJSON(&msg); err != nil {
		t.Fatalf("read: %v", err)
	}
	return msg
}

func sendText(t *testing.T, conn *websocket.Conn, text string) {
	t.Helper()
	if err := conn.WriteMessage(websocket.TextMessage, []byte(text)); err != nil {
		t.Fatalf("write: %v", err)
	}
}

func TestHello(t *testing.T) {
	conn := connect(t, normalInterval)

	msg := readMessage(t, conn)
	if msg.Op != protocol.OpHello {
		t.Fatalf("op = %q, want %q", msg.Op, protocol.OpHello)
	}

	var d protocol.HelloData
	if err := json.Unmarshal(msg.D, &d); err != nil {
		t.Fatalf("unmarshal hello d: %v", err)
	}
	if d.HeartbeatInterval != 30000 {
		t.Errorf("heartbeat_interval = %d, want 30000", d.HeartbeatInterval)
	}
}

func TestPingPong(t *testing.T) {
	conn := connectReady(t, normalInterval)

	sendText(t, conn, `{"op":"ping"}`)
	msg := readMessage(t, conn)

	if msg.Op != protocol.OpPong {
		t.Errorf("op = %q, want %q", msg.Op, protocol.OpPong)
	}
	if len(msg.D) != 0 {
		t.Errorf("pong must have no d, got %s", msg.D)
	}
}

func TestEcho(t *testing.T) {
	conn := connectReady(t, normalInterval)

	sendText(t, conn, `{"op":"echo","d":{"text":"привет"}}`)
	msg := readMessage(t, conn)

	if msg.Op != protocol.OpEcho {
		t.Fatalf("op = %q, want %q", msg.Op, protocol.OpEcho)
	}

	var d struct {
		Text string `json:"text"`
	}
	if err := json.Unmarshal(msg.D, &d); err != nil {
		t.Fatalf("unmarshal echo d: %v", err)
	}
	if d.Text != "привет" {
		t.Errorf("text = %q, want %q", d.Text, "привет")
	}
}

func TestErrors(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		wantCode string
	}{
		{"bad json", `привет`, protocol.ErrBadJSON},
		{"no op", `{}`, protocol.ErrNoOp},
		{"unknown op", `{"op":"abc"}`, protocol.ErrUnknownOp},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			conn := connectReady(t, normalInterval)

			sendText(t, conn, tt.input)
			msg := readMessage(t, conn)

			if msg.Op != protocol.OpError {
				t.Fatalf("op = %q, want %q", msg.Op, protocol.OpError)
			}
			var d protocol.ErrorData
			if err := json.Unmarshal(msg.D, &d); err != nil {
				t.Fatalf("unmarshal error d: %v", err)
			}
			if d.Code != tt.wantCode {
				t.Errorf("code = %q, want %q", d.Code, tt.wantCode)
			}
			if d.Message == "" {
				t.Error("message must not be empty")
			}

			sendText(t, conn, `{"op":"ping"}`)
			if msg := readMessage(t, conn); msg.Op != protocol.OpPong {
				t.Errorf("after error: op = %q, want %q", msg.Op, protocol.OpPong)
			}
		})
	}
}

func TestHeartbeatTimeoutDisconnects(t *testing.T) {
	conn := connectReady(t, fastInterval)

	conn.SetReadDeadline(time.Now().Add(2 * time.Second))
	_, _, err := conn.ReadMessage()

	if err == nil {
		t.Fatal("expected connection to be closed, but got a message")
	}
	var netErr net.Error
	if errors.As(err, &netErr) && netErr.Timeout() {
		t.Fatal("server did not close a silent client within 2s")
	}
}

func TestHeartbeatPingKeepsAlive(t *testing.T) {
	conn := connectReady(t, fastInterval)

	for i := 0; i < 6; i++ {
		time.Sleep(fastInterval)
		sendText(t, conn, `{"op":"ping"}`)
		if msg := readMessage(t, conn); msg.Op != protocol.OpPong {
			t.Fatalf("ping #%d: op = %q, want %q", i+1, msg.Op, protocol.OpPong)
		}
	}
}
