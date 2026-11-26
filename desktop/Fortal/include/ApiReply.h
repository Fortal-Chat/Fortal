#pragma once

#include <QObject>
#include <QNetworkReply>

class ApiReply : public QObject {
    Q_OBJECT

public:
    ApiReply(QNetworkReply* reply, bool authorized, QObject* parent);

signals:
    void succeeded(const QByteArray& body);
    void failed(const QString& code, const QString& message);
    void unauthorized();

private:
    QNetworkReply* m_reply;
    bool m_authorized;

private slots:
    void onFinished();
};
