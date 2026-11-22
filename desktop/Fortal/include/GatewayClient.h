#pragma once

#include <QObject>
#include <QtQml/qqmlregistration.h>
#include <QWebSocket>
#include <QTimer>

#include <memory>

#include "Backoff.h"
#include "Protocol.h"

class GatewayClient : public QObject{
    Q_OBJECT
    QML_ELEMENT
    Q_PROPERTY(connectionStatus status READ status NOTIFY statusChanged)
    Q_PROPERTY(QUrl url READ url WRITE setUrl NOTIFY urlChanged)

public:
    Q_INVOKABLE void start();
    Q_INVOKABLE void stop();

    enum connectionStatus{
        Disconnected,
        Connecting,
        Connected,
        Reconnecting
    };
    Q_ENUM(connectionStatus)

    explicit GatewayClient(int helloTime = 10'000, QObject *parent = nullptr);
    ~GatewayClient() override;

    connectionStatus status() const;
    QUrl url() const;
    void setUrl(const QUrl& url);

signals:
    void statusChanged();
    void urlChanged();

private:
    std::unique_ptr<QWebSocket> m_socket;
    QUrl m_url;
    std::unique_ptr<QTimer> m_heartbeatTimer;
    std::unique_ptr<QTimer> m_reconnectTimer;
    std::unique_ptr<QTimer> m_connectTimer;
    Backoff m_backoff;
    int m_helloTime;
    connectionStatus m_connectionStatus = connectionStatus::Disconnected;
    bool m_awaitingPong = false;
    bool m_wantConnected = false;

    void scheduleReconnect();
    void setStatus(connectionStatus new_status);

private slots:
    void onConnected();
    void onTextMessageReceived(const QString& message);
    void onClosed();
    void onErrorOccurred();
    void onReconnectTimeOut();
    void onHeartbeatTimeOut();
    void onConnectTimeOut();
};
