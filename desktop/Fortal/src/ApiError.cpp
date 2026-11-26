#include "ApiError.h"

#include <QJsonDocument>
#include <QJsonObject>

#include <optional>

static std::optional<ApiError> errorFromObj(const QJsonObject& obj) {
    const QString code = obj.value("code").toString();
    const QString message = obj.value("message").toString();
    if(code.isEmpty()) return std::nullopt;
    return ApiError{code, message};
}

ApiError ApiError::fromBody(const QByteArray& body) {
    const QJsonObject obj = QJsonDocument::fromJson(body).object();
    return errorFromObj(obj.value("error").toObject()).value_or(ApiError{"bad_response", "Некорректный ответ сервера"});
}
