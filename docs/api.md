# Договор API — v0

> Меняется **только** через Pull Request, который одобрили оба.
> Клиенты, пока сервер не готов, работают с заглушками по этому документу.

Общее:
- Формат: JSON, кодировка UTF-8, `Content-Type: application/json`.
- Время: строка ISO 8601 в UTC, например `"2026-11-07T12:00:00Z"`.
- ID: строка (UUID), например `"7f1c…"`.
- Ошибка (любой HTTP-код 4xx/5xx):
  ```json
  { "error": { "code": "invalid_credentials", "message": "Неверный логин или пароль" } }
  ```
  `code` — стабильный машинный код, `message` — для человека (может меняться).

Адреса (dev):

| Сервис | Порт | Пример |
|---|---|---|
| Сервер (Ktor), HTTP | 8080 | `http://localhost:8080` |
| Gateway, WebSocket | 8081 | `ws://localhost:8081/` |

---

## HTTP (сервер)

### `GET /health`
Проверка, что сервер жив.

`200`
```json
{ "status": "ok" }
```

### `POST /auth/register`
Запрос:
```json
{ "username": "alice", "email": "alice@example.com", "password": "secret123" }
```
Правила: `username` 4–32 символа `[a-zA-Z0-9_]`, `password` ≥ 8 символов.

`201`
```json
{ "user": { "id": "uuid", "username": "alice" }, "token": "<JWT>" }
```
Ошибки: `400 validation_error`, `409 username_taken`, `409 email_taken`.

### `POST /auth/login`
Запрос:
```json
{ "login": "alice", "password": "secret123" }
```
`login` — username или email.

`200` — тело как у `/auth/register`.
Ошибки: `400 validation_error`, `401 invalid_credentials`.

Дальнейшие запросы передают токен заголовком `Authorization: Bearer <JWT>`.

---

## Gateway (WebSocket)

Подключение: `ws://<host>:8081/`. Каждое сообщение — текстовый кадр с JSON вида:
```json
{ "op": "<операция>", "d": <данные или отсутствует> }
```

### Сервер → клиент: `hello`
Отправляется сразу после подключения.
```json
{ "op": "hello", "d": { "heartbeat_interval": 30000 } }
```
`heartbeat_interval` — как часто (мс) клиент должен слать `ping`.

### Клиент → сервер: `ping` → ответ `pong`
```json
{ "op": "ping" }
```
```json
{ "op": "pong" }
```

### Клиент → сервер: `echo` (только v0, для проверки связи)
```json
{ "op": "echo", "d": { "text": "привет" } }
```
Ответ — те же `d`:
```json
{ "op": "echo", "d": { "text": "привет" } }
```

### Сервер → клиент: `error`
```json
{ "op": "error", "d": { "code": "unknown_op", "message": "..." } }
```
Коды: `bad_json`, `no_op`, `unknown_op`.

### Позже (не v0)
- `identify` с JWT после `hello`
- События `message_create` и т.п. из Redis

---

## История изменений
- **v0** — `/health`, регистрация, вход, gateway `hello`/`ping`/`echo`. 11.11.2026

