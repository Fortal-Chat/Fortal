#include "AuthProtocol.h"

#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonParseError>

static std::optional<AuthProtocol::User> userFromObj(const QJsonObject& obj) {
    const QString id = obj.value("id").toString();
    const QString username = obj.value("username").toString();
    if(id.isEmpty() || username.isEmpty()) return std::nullopt;
    return AuthProtocol::User{id, username};
}

static std::optional<AuthProtocol::ApiError> errorFromObj(const QJsonObject& obj) {
    const QString code = obj.value("code").toString();
    const QString message = obj.value("message").toString();
    if(code.isEmpty()) return std::nullopt;
    return AuthProtocol::ApiError{code, message};
}


QByteArray AuthProtocol::registerBody(const QString& username, const QString& email, const QString& password) {
    QJsonObject result;
    result.insert("username", username);
    result.insert("email", email);
    result.insert("password", password);
    return QJsonDocument(result).toJson(QJsonDocument::Compact);
}

QByteArray AuthProtocol::loginBody(const QString& login, const QString& password) {
    QJsonObject result;
    result.insert("login", login);
    result.insert("password", password);
    return QJsonDocument(result).toJson(QJsonDocument::Compact);
}

std::optional<AuthProtocol::Session> AuthProtocol::parseSession(const QByteArray& body) {
    QJsonParseError err;
    const QJsonDocument doc = QJsonDocument::fromJson(body, &err);
    if (!doc.isObject() || err.error != QJsonParseError::NoError) return std::nullopt;

    const QJsonObject obj = doc.object();
    const auto user = userFromObj(obj.value("user").toObject());
    const auto token = obj.value("token").toString();
    if (token.isEmpty() || !user) return std::nullopt;

    return Session{*user, token};
}

AuthProtocol::ApiError AuthProtocol::parseError(const QByteArray& body) {
    const QJsonObject obj = QJsonDocument::fromJson(body).object();
    return errorFromObj(obj.value("error").toObject()).value_or(ApiError{"bad_response", "Некорректный ответ сервера"});
}