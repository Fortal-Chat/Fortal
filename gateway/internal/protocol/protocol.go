package protocol

import "encoding/json"

const (
	OpHello = "hello"
	OpPing  = "ping"
	OpPong  = "pong"
	OpEcho  = "echo"
	OpError = "error"
)

const (
	ErrBadJSON   = "bad_json"
	ErrNoOp      = "no_op"
	ErrUnknownOp = "unknown_op"
)

type Message struct {
	Op string          `json:"op"`
	D  json.RawMessage `json:"d,omitempty"`
}

type HelloData struct {
	HeartbeatInterval int `json:"heartbeat_interval"`
}

type ErrorData struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func New(op string, d any) (Message, error) {
	if d == nil {
		return Message{Op: op}, nil
	}
	raw, err := json.Marshal(d)
	if err != nil {
		return Message{}, err
	}
	return Message{Op: op, D: raw}, nil
}
