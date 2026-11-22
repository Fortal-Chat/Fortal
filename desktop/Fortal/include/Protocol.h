#pragma once

#include <QString>
#include <QJsonObject>

#include <optional>

class Protocol {
public:
    enum class Op {
        Hello,
        Ping,
        Pong,
        Error,
        Unknown
    };

    struct Message {
        QJsonObject d;
        Op op;
    };

    static std::optional<Message> parse(const QString& message);
    static QString serialize(Op op, const QJsonObject& d = {});
};
