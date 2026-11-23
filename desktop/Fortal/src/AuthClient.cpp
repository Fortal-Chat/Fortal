#include "AuthClient.h"

#include <QNetworkRequest>
#include <QDebug>

AuthClient::AuthClient(QObject* parent)
    : QObject(parent), m_network(std::make_unique<QNetworkAccessManager>())
{
}

AuthClient::~AuthClient() {
}

void AuthClient::login(const QString& login, const QString& password) {
}

void AuthClient::registerUser(const QString& username, const QString& email, const QString& password) {
}

void AuthClient::logout() {
}

QUrl AuthClient::baseUrl() const {
    return m_baseUrl;
}

void AuthClient::setBaseUrl(const QUrl& url) {
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
}

void AuthClient::onFinished() {
}
