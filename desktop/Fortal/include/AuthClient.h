#pragma once

#include <QObject>
#include <QtQml/qqmlregistration.h>
#include <QPointer>

#include "ApiClient.h"
#include "AuthProtocol.h"

class AuthClient : public QObject {
    Q_OBJECT
    QML_ELEMENT
    Q_PROPERTY(ApiClient* api READ api WRITE setApi NOTIFY apiChanged)
    Q_PROPERTY(bool busy READ busy NOTIFY busyChanged)
    Q_PROPERTY(QString token READ token NOTIFY sessionChanged)
    Q_PROPERTY(QString username READ username NOTIFY sessionChanged)

public:
    explicit AuthClient(QObject* parent = nullptr);

    Q_INVOKABLE void login(const QString& login, const QString& password);
    Q_INVOKABLE void registerUser(const QString& username, const QString& email, const QString& password);
    Q_INVOKABLE void logout();

    ApiClient* api() const;
    void setApi(ApiClient* api);
    bool busy() const;
    QString token() const;
    QString username() const;

signals:
    void apiChanged();
    void busyChanged();
    void sessionChanged();
    void loggedIn();
    void failed(const QString& code, const QString& message);
    void sessionExpired();

private:
    QPointer<ApiClient> m_api;
    ApiReply* m_reply = nullptr;
    AuthProtocol::Session m_session;

private slots:
    void onSessionReceived(const QByteArray& body);
    void onRequestFailed(const QString& code, const QString& message);
    void onUnauthorized();
};
