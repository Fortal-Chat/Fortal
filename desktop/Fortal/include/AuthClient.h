#pragma once

#include <QObject>
#include <QtQml/qqmlregistration.h>
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QUrl>

#include <memory>

#include "AuthProtocol.h"

class AuthClient : public QObject {
    Q_OBJECT
    QML_ELEMENT
    Q_PROPERTY(QUrl baseUrl READ baseUrl WRITE setBaseUrl NOTIFY baseUrlChanged)
    Q_PROPERTY(bool busy READ busy NOTIFY busyChanged)
    Q_PROPERTY(QString token READ token NOTIFY sessionChanged)
    Q_PROPERTY(QString username READ username NOTIFY sessionChanged)

public:
    explicit AuthClient(QObject* parent = nullptr);
    ~AuthClient() override;

    Q_INVOKABLE void login(const QString& login, const QString& password);
    Q_INVOKABLE void registerUser(const QString& username, const QString& email, const QString& password);
    Q_INVOKABLE void logout();

    QUrl baseUrl() const;
    void setBaseUrl(const QUrl& url);
    bool busy() const;
    QString token() const;
    QString username() const;

signals:
    void baseUrlChanged();
    void busyChanged();
    void sessionChanged();
    void loggedIn();
    void failed(const QString& code, const QString& message);

private:
    std::unique_ptr<QNetworkAccessManager> m_network;
    QNetworkReply* m_reply = nullptr;
    QUrl m_baseUrl;
    AuthProtocol::Session m_session;

    void post(const QString& path, const QByteArray& body);

private slots:
    void onFinished();
};
