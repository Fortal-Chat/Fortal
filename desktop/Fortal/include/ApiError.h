#pragma once

#include <QByteArray>
#include <QString>

struct ApiError {
    QString code;
    QString message;

    static ApiError fromBody(const QByteArray& body);
};
