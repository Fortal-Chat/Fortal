#pragma once

#include <QObject>
#include <QtQml/qqmlregistration.h>
#include <QNetworkAccessManager>
#include <QUrl>

#include <memory>

#include "ApiReply.h"

class ApiClient : public QObject {
    Q_OBJECT
    QML_ELEMENT
    Q_PROPERTY(QUrl baseUrl READ baseUrl WRITE setBaseUrl NOTIFY baseUrlChanged)

public:
    explicit ApiClient(QObject* parent = nullptr);

    ApiReply* get(const QString& path);
    ApiReply* post(const QString& path, const QByteArray& body);
    ApiReply* patch(const QString& path, const QByteArray& body);
    ApiReply* remove(const QString& path);

    QUrl baseUrl() const;
    void setBaseUrl(const QUrl& url);
    QString token() const;
    void setToken(const QString& token);

signals:
    void baseUrlChanged();
    void unauthorized();

private:
    std::unique_ptr<QNetworkAccessManager> m_network;
    QUrl m_baseUrl;
    QString m_token;

    ApiReply* send(const QByteArray& method, const QString& path, const QByteArray& body = {});
};
