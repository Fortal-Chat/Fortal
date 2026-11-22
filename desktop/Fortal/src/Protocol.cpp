#include "Protocol.h"
#include <QJsonDocument>
#include <QJsonParseError>
#include <QJsonValue>

static Protocol::Op opFromString(const QString& s) {
    if (s == "hello") return Protocol::Op::Hello;
    if (s == "ping") return Protocol::Op::Ping;
    if (s == "pong") return Protocol::Op::Pong;
    if (s == "error") return Protocol::Op::Error;
    return Protocol::Op::Unknown;
}

static QString opToString(Protocol::Op op) {
    switch (op) {
        case Protocol::Op::Hello: return "hello";
        case Protocol::Op::Ping: return "ping";
        case Protocol::Op::Pong: return "pong";
        case Protocol::Op::Error: return "error";
        case Protocol::Op::Unknown: return {};
    }
    return {};
}

QString Protocol::serialize(Op op, const QJsonObject& d) {
    QJsonObject obj;
    obj.insert("op", opToString(op));
    if (!d.isEmpty()) {
        obj.insert("d", d);
    }
    return QString::fromUtf8(QJsonDocument(obj).toJson(QJsonDocument::Compact));
}

std::optional<Protocol::Message> Protocol::parse(const QString& message) {
    QJsonParseError err;
    const QJsonDocument doc = QJsonDocument::fromJson(message.toUtf8(), &err);
    if (!doc.isObject() || err.error != QJsonParseError::NoError) return std::nullopt;

    const QJsonObject obj = doc.object();
    const QJsonValue opValue = obj.value("op");
    if (!opValue.isString() || opValue.toString().isEmpty()) return std::nullopt;

    Message msg;
    msg.op = opFromString(opValue.toString());
    msg.d = obj.value("d").toObject();
    return msg;
}