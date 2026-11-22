#include <QTest>
#include <QJsonDocument>
#include <QJsonObject>

#include "Protocol.h"

Q_DECLARE_METATYPE(Protocol::Op)

static QJsonObject toObject(const QString& json) {
    return QJsonDocument::fromJson(json.toUtf8()).object();
}

class TestProtocol : public QObject {
    Q_OBJECT

private slots:
    void parseOp_data();
    void parseOp();
    void parseRejectsInvalid_data();
    void parseRejectsInvalid();
    void parseHelloData();
    void parseMissingDataIsEmpty();
    void parseNonObjectDataIsEmpty();
    void serializeWithoutData();
    void serializeWithData();
    void roundTrip();
};

void TestProtocol::parseOp_data() {
    QTest::addColumn<QString>("message");
    QTest::addColumn<Protocol::Op>("op");

    QTest::newRow("hello") << QString(R"({"op":"hello","d":{"heartbeat_interval":30000}})") << Protocol::Op::Hello;
    QTest::newRow("ping") << QString(R"({"op":"ping"})") << Protocol::Op::Ping;
    QTest::newRow("pong") << QString(R"({"op":"pong"})") << Protocol::Op::Pong;
    QTest::newRow("error") << QString(R"({"op":"error","d":{"code":"bad_json","message":"invalid JSON"}})") << Protocol::Op::Error;
    QTest::newRow("unknown") << QString(R"({"op":"message_create","d":{}})") << Protocol::Op::Unknown;
}

void TestProtocol::parseOp() {
    QFETCH(QString, message);
    QFETCH(Protocol::Op, op);

    const auto msg = Protocol::parse(message);
    QVERIFY(msg.has_value());
    QCOMPARE(msg->op, op);
}

void TestProtocol::parseRejectsInvalid_data() {
    QTest::addColumn<QString>("message");

    QTest::newRow("not json") << QString("not json");
    QTest::newRow("empty") << QString("");
    QTest::newRow("array") << QString("[1,2]");
    QTest::newRow("string") << QString(R"("hello")");
    QTest::newRow("no op") << QString(R"({"d":{}})");
    QTest::newRow("op is number") << QString(R"({"op":5})");
    QTest::newRow("op is empty") << QString(R"({"op":""})");
}

void TestProtocol::parseRejectsInvalid() {
    QFETCH(QString, message);

    QVERIFY(!Protocol::parse(message).has_value());
}

void TestProtocol::parseHelloData() {
    const auto msg = Protocol::parse(R"({"op":"hello","d":{"heartbeat_interval":30000}})");
    QVERIFY(msg.has_value());
    QCOMPARE(msg->d.value("heartbeat_interval").toInt(), 30000);
}

void TestProtocol::parseMissingDataIsEmpty() {
    const auto msg = Protocol::parse(R"({"op":"pong"})");
    QVERIFY(msg.has_value());
    QVERIFY(msg->d.isEmpty());
}

void TestProtocol::parseNonObjectDataIsEmpty() {
    const auto msg = Protocol::parse(R"({"op":"pong","d":"text"})");
    QVERIFY(msg.has_value());
    QVERIFY(msg->d.isEmpty());
}

void TestProtocol::serializeWithoutData() {
    const QJsonObject obj = toObject(Protocol::serialize(Protocol::Op::Ping));
    QCOMPARE(obj.value("op").toString(), QString("ping"));
    QVERIFY(!obj.contains("d"));
}

void TestProtocol::serializeWithData() {
    const QJsonObject d{{"code", "bad_json"}, {"message", "invalid JSON"}};
    const QJsonObject obj = toObject(Protocol::serialize(Protocol::Op::Error, d));
    QCOMPARE(obj.value("op").toString(), QString("error"));
    QCOMPARE(obj.value("d").toObject(), d);
}

void TestProtocol::roundTrip() {
    const auto msg = Protocol::parse(Protocol::serialize(Protocol::Op::Pong));
    QVERIFY(msg.has_value());
    QCOMPARE(msg->op, Protocol::Op::Pong);
    QVERIFY(msg->d.isEmpty());
}

QTEST_APPLESS_MAIN(TestProtocol)
#include "tst_protocol.moc"
