FROM golang:1.24-alpine AS builder

WORKDIR /src

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 go build -o /out/gateway ./cmd/gateway

FROM alpine:3.21

RUN adduser -D -H app
USER app

COPY --from=builder /out/gateway /usr/local/bin/gateway

EXPOSE 8081
ENTRYPOINT ["gateway"]

