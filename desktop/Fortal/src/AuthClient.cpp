#include "AuthClient.h"

AuthClient::AuthClient(QObject* parent) : QObject(parent) {}

void AuthClient::login(const QString& login, const QString& password) {
    if (busy() || !m_api) return;
    m_reply = m_api->post("/auth/login", AuthProtocol::loginBody(login, password));
    connect(m_reply, &ApiReply::succeeded, this, &AuthClient::onSessionReceived);
    connect(m_reply, &ApiReply::failed, this, &AuthClient::onRequestFailed);
    emit busyChanged();
}

void AuthClient::registerUser(const QString& username, const QString& email, const QString& password) {
    if (busy() || !m_api) return;
    m_reply = m_api->post("/auth/register", AuthProtocol::registerBody(username, email, password));
    connect(m_reply, &ApiReply::succeeded, this, &AuthClient::onSessionReceived);
    connect(m_reply, &ApiReply::failed, this, &AuthClient::onRequestFailed);
    emit busyChanged();
}

void AuthClient::logout() {
    m_session = AuthProtocol::Session{};
    if (m_api) m_api->setToken({});
    emit sessionChanged();
}

ApiClient* AuthClient::api() const {
    return m_api;
}

void AuthClient::setApi(ApiClient* api) {
    if (api == m_api) return;
    if (m_api) m_api->disconnect(this);
    m_api = api;
    if (m_api) connect(m_api, &ApiClient::unauthorized, this, &AuthClient::onUnauthorized);
    emit apiChanged();
}

bool AuthClient::busy() const {
    return m_reply != nullptr;
}

QString AuthClient::token() const {
    return m_session.token;
}

QString AuthClient::username() const {
    return m_session.user.username;
}

void AuthClient::onSessionReceived(const QByteArray& body) {
    m_reply = nullptr;
    emit busyChanged();

    const auto session = AuthProtocol::parseSession(body);
    if(!session) {
        emit failed("bad_response", "Некорректный ответ сервера");
        return;
    }
    m_session = *session;
    if (m_api) m_api->setToken(m_session.token);
    emit sessionChanged();
    emit loggedIn();
}

void AuthClient::onRequestFailed(const QString& code, const QString& message) {
    m_reply = nullptr;
    emit busyChanged();
    emit failed(code, message);
}

void AuthClient::onUnauthorized() {
    logout();
    emit sessionExpired();
}
