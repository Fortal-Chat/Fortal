#include "ApiReply.h"
#include "ApiError.h"

#include <QNetworkRequest>

ApiReply::ApiReply(QNetworkReply* reply, bool authorized, QObject* parent) : QObject(parent), m_reply(reply), m_authorized(authorized) {
    connect(m_reply, &QNetworkReply::finished, this, &ApiReply::onFinished);
}

void ApiReply::onFinished() {
    const int status = m_reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    const QByteArray body = m_reply->readAll();
    m_reply->deleteLater();
    m_reply = nullptr;
    deleteLater();

    if (status == 0) {
        emit failed("network_error", "Сервер недоступен");
        return;
    }
    if (status >= 200 && status < 300) {
        emit succeeded(body);
        return;
    }
    if (status == 401 && m_authorized) {
        emit unauthorized();
    }
    const ApiError error = ApiError::fromBody(body);
    emit failed(error.code, error.message);
}
