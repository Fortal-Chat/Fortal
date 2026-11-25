#include "AuthClient.h"

#include <QNetworkRequest>
#include <QDebug>

AuthClient::AuthClient(QObject* parent) : QObject(parent), m_network(std::make_unique<QNetworkAccessManager>()) {}

AuthClient::~AuthClient() {
    if(!m_reply) return;
    m_reply->disconnect(this);
    m_reply->abort();
}

void AuthClient::login(const QString& login, const QString& password) {
    if(busy()) return;
    post("/auth/login", AuthProtocol::loginBody(login, password));
}

void AuthClient::registerUser(const QString& username, const QString& email, const QString& password) {
    if(busy()) return;
    post("/auth/register", AuthProtocol::registerBody(username, email, password));
}

void AuthClient::logout() {
    m_session = AuthProtocol::Session{};
    emit sessionChanged();
}

QUrl AuthClient::baseUrl() const {
    return m_baseUrl;
}

void AuthClient::setBaseUrl(const QUrl& url) {
    if(url.isEmpty() || url == m_baseUrl) return;
    m_baseUrl = url;
    emit baseUrlChanged();
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

void AuthClient::post(const QString& path, const QByteArray& body) {
    QNetworkRequest request(m_baseUrl.resolved(QUrl(path)));
    request.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");
    request.setTransferTimeout(10'000);

    m_reply = m_network->post(request, body);
    connect(m_reply, &QNetworkReply::finished, this, &AuthClient::onFinished);
    emit busyChanged();
}

void AuthClient::onFinished() {
    const int status = m_reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    const QByteArray body = m_reply->readAll();
    m_reply->deleteLater();
    m_reply = nullptr;
    emit busyChanged();

    if (status == 0) {
        handleNetworkError();
        return;
    }
    if (status >= 200 && status < 300) {
        handleSuccess(body);
        return;
    }
    handleFailure(body);
}

void AuthClient::handleSuccess(const QByteArray& body) {
    const auto session = AuthProtocol::parseSession(body);
    if(!session) {
        emit failed("bad_response", "Некорректный ответ сервера");
        return;
    }
    m_session = *session;
    emit sessionChanged();
    emit loggedIn();
}

void AuthClient::handleFailure(const QByteArray& body) {
    const auto error = AuthProtocol::parseError(body);
    emit failed(error.code, error.message);
}

void AuthClient::handleNetworkError() {
    emit failed("network_error", "Сервер недоступен");
}
