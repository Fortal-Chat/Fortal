#pragma once

#include <QByteArray>
#include <QString>

#include <optional>

class AuthProtocol {
public:
    struct User {
        QString id;
        QString username;
    };

    struct Session {
        User user;
        QString token;
    };

    static QByteArray registerBody(const QString& username, const QString& email, const QString& password);
    static QByteArray loginBody(const QString& login, const QString& password);
    static std::optional<Session> parseSession(const QByteArray& body);
};
