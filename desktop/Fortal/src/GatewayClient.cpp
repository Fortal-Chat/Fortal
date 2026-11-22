#include "GatewayClient.h"

#include <QDebug>

GatewayClient::GatewayClient(int helloTime, QObject *parent)
    : QObject(parent), m_socket(std::make_unique<QWebSocket>()),
    m_heartbeatTimer(std::make_unique<QTimer>()), m_reconnectTimer(std::make_unique<QTimer>()),
    m_connectTimer(std::make_unique<QTimer>()), m_helloTime(helloTime)
{
    connect(m_socket.get(), &QWebSocket::connected, this, &GatewayClient::onConnected);
    connect(m_socket.get(), &QWebSocket::textMessageReceived, this, &GatewayClient::onTextMessageReceived);
    connect(m_socket.get(), &QWebSocket::disconnected, this, &GatewayClient::onClosed);
    connect(m_socket.get(), &QWebSocket::errorOccurred, this, &GatewayClient::onErrorOccurred);
    connect(m_heartbeatTimer.get(), &QTimer::timeout, this, &GatewayClient::onHeartbeatTimeOut);
    connect(m_reconnectTimer.get(), &QTimer::timeout, this, &GatewayClient::onReconnectTimeOut);
    connect(m_connectTimer.get(), &QTimer::timeout, this, &GatewayClient::onConnectTimeOut);

    m_reconnectTimer->setSingleShot(true);
    m_connectTimer->setSingleShot(true);
}

GatewayClient::~GatewayClient() {
    m_socket->disconnect(this);
}

void GatewayClient::start() {
    if(!m_url.isValid()) {
        qWarning() << "GatewayClient: url is not valid";
        return;
    }
    if(m_wantConnected) return;
    if(m_socket->state() != QAbstractSocket::UnconnectedState) {
        m_socket->abort();
    }

    m_wantConnected = true;
    m_reconnectTimer->stop();
    m_backoff.reset();
    setStatus(Connecting);
    m_socket->open(m_url);
    m_connectTimer->start(m_helloTime);
}

void GatewayClient::stop() {
    m_wantConnected = false;
    m_heartbeatTimer->stop();
    m_reconnectTimer->stop();
    m_connectTimer->stop();
    m_awaitingPong = false;
    m_socket->close();
    setStatus(Disconnected);
}


void GatewayClient::onConnected() {
    qDebug() << "GatewayClient: Connection set";
}

void GatewayClient::onTextMessageReceived(const QString& message) {
    const auto msg = Protocol::parse(message);
    if(!msg) return;

    using PO = Protocol::Op;
    switch (msg->op) {
        case PO::Hello:
            {
                m_connectTimer->stop();
                const int interval = msg->d.value("heartbeat_interval").toInt();
                if(interval <= 0) {
                    qWarning() << "GatewayClient: invalid heartbeat time";
                    m_socket->abort();
                    break;
                }

                m_awaitingPong = false;
                m_heartbeatTimer->start(interval);
                m_backoff.reset();
                setStatus(Connected);
            }
            break;
        case PO::Pong:
            m_awaitingPong = false;
            break;
        case PO::Error:
            qWarning() << "GatewayClient:" << msg->d.value("code").toString() << msg->d.value("message").toString();
            break;
        case PO::Ping:
        case PO::Unknown:
            break;
    }
}

void GatewayClient::onClosed() {
    qDebug() << "GatewayClient: disconnected";
    m_heartbeatTimer->stop();
    m_connectTimer->stop();
    m_awaitingPong = false;

    if (!m_wantConnected) {
        setStatus(Disconnected);
        return;
    }
    scheduleReconnect();
}

void GatewayClient::onErrorOccurred() {
    qWarning() << "GatewayClient: error:" << m_socket->errorString();

    if (m_socket->state() == QAbstractSocket::UnconnectedState) {
        m_heartbeatTimer->stop();
        m_connectTimer->stop();
        m_awaitingPong = false;
        scheduleReconnect();
    }
}

void GatewayClient::scheduleReconnect() {
    if (!m_wantConnected || m_reconnectTimer->isActive()) return;

    const int delay = m_backoff.next();
    qDebug() << "GatewayClient: reconnect in" << delay << "ms";
    setStatus(Reconnecting);
    m_reconnectTimer->start(delay);
}

void GatewayClient::onReconnectTimeOut() {
    if (!m_wantConnected) return;

    setStatus(Connecting);
    m_socket->open(m_url);
    m_connectTimer->start(m_helloTime);
}

void GatewayClient::onHeartbeatTimeOut() {
    if (m_awaitingPong) {
        qWarning() << "GatewayClient: no pong connection is dead";
        m_socket->abort();
        return;
    }

    m_socket->sendTextMessage(Protocol::serialize(Protocol::Op::Ping));
    m_awaitingPong = true;
}

void GatewayClient::onConnectTimeOut() {
    qWarning() << "GatewayClient: connection time out";
    m_socket->abort();
    scheduleReconnect();
}

GatewayClient::connectionStatus GatewayClient::status() const {
    return m_connectionStatus;
}

void GatewayClient::setStatus(connectionStatus new_status) {
    if(new_status == m_connectionStatus) return;

    m_connectionStatus = new_status;
    emit statusChanged();
}

QUrl GatewayClient::url() const {
    return m_url;
}

void GatewayClient::setUrl(const QUrl& url) {
    if(url.isEmpty() || url == m_url) return;
    m_url = url;
    emit urlChanged();
}

