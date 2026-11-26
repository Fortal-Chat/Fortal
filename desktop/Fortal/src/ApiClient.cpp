#include "ApiClient.h"

#include <QNetworkRequest>

ApiClient::ApiClient(QObject* parent) : QObject(parent), m_network(std::make_unique<QNetworkAccessManager>()) {}

ApiReply* ApiClient::get(const QString& path) {
    return send("GET", path);
}

ApiReply* ApiClient::post(const QString& path, const QByteArray& body) {
    return send("POST", path, body);
}

ApiReply* ApiClient::patch(const QString& path, const QByteArray& body) {
    return send("PATCH", path, body);
}

ApiReply* ApiClient::remove(const QString& path) {
    return send("DELETE", path);
}

QUrl ApiClient::baseUrl() const {
    return m_baseUrl;
}

void ApiClient::setBaseUrl(const QUrl& url) {
    if(url.isEmpty() || url == m_baseUrl) return;
    m_baseUrl = url;
    emit baseUrlChanged();
}

QString ApiClient::token() const {
    return m_token;
}

void ApiClient::setToken(const QString& token) {
    m_token = token;
}

ApiReply* ApiClient::send(const QByteArray& method, const QString& path, const QByteArray& body) {
    QNetworkRequest request(m_baseUrl.resolved(QUrl(path)));
    request.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");
    request.setTransferTimeout(10'000);

    const bool authorized = !m_token.isEmpty();
    if (authorized) {
        request.setRawHeader("Authorization", "Bearer " + m_token.toUtf8());
    }

    QNetworkReply* networkReply = m_network->sendCustomRequest(request, method, body);
    auto* reply = new ApiReply(networkReply, authorized, this);
    connect(reply, &ApiReply::unauthorized, this, &ApiClient::unauthorized);
    return reply;
}
