#include <QTest>
#include <QJsonDocument>
#include <QJsonObject>

#include "AuthProtocol.h"

static QJsonObject toObject(const QByteArray& json) {
    return QJsonDocument::fromJson(json).object();
}

class TestAuthProtocol : public QObject {
    Q_OBJECT

private slots:
    void registerBodyFields();
    void loginBodyFields();
    void parseSessionValid();
    void parseSessionRejectsInvalid_data();
    void parseSessionRejectsInvalid();
};

void TestAuthProtocol::registerBodyFields() {
    const QJsonObject obj = toObject(AuthProtocol::registerBody("alice", "alice@example.com", "secret123"));
    QCOMPARE(obj.size(), 3);
    QCOMPARE(obj.value("username").toString(), QString("alice"));
    QCOMPARE(obj.value("email").toString(), QString("alice@example.com"));
    QCOMPARE(obj.value("password").toString(), QString("secret123"));
}

void TestAuthProtocol::loginBodyFields() {
    const QJsonObject obj = toObject(AuthProtocol::loginBody("alice", "secret123"));
    QCOMPARE(obj.size(), 2);
    QCOMPARE(obj.value("login").toString(), QString("alice"));
    QCOMPARE(obj.value("password").toString(), QString("secret123"));
}

void TestAuthProtocol::parseSessionValid() {
    const auto session = AuthProtocol::parseSession(R"({"user":{"id":"7f1c","username":"alice"},"token":"jwt.token.here"})");
    QVERIFY(session.has_value());
    QCOMPARE(session->user.id, QString("7f1c"));
    QCOMPARE(session->user.username, QString("alice"));
    QCOMPARE(session->token, QString("jwt.token.here"));
}

void TestAuthProtocol::parseSessionRejectsInvalid_data() {
    QTest::addColumn<QByteArray>("body");

    QTest::newRow("not json") << QByteArray("not json");
    QTest::newRow("empty") << QByteArray("");
    QTest::newRow("array") << QByteArray("[1,2]");
    QTest::newRow("no user") << QByteArray(R"({"token":"t"})");
    QTest::newRow("user is string") << QByteArray(R"({"user":"alice","token":"t"})");
    QTest::newRow("user without id") << QByteArray(R"({"user":{"username":"alice"},"token":"t"})");
    QTest::newRow("user without username") << QByteArray(R"({"user":{"id":"7f1c"},"token":"t"})");
    QTest::newRow("no token") << QByteArray(R"({"user":{"id":"7f1c","username":"alice"}})");
    QTest::newRow("empty token") << QByteArray(R"({"user":{"id":"7f1c","username":"alice"},"token":""})");
    QTest::newRow("token is number") << QByteArray(R"({"user":{"id":"7f1c","username":"alice"},"token":5})");
    QTest::newRow("error body") << QByteArray(R"({"error":{"code":"invalid_credentials","message":"x"}})");
}

void TestAuthProtocol::parseSessionRejectsInvalid() {
    QFETCH(QByteArray, body);

    QVERIFY(!AuthProtocol::parseSession(body).has_value());
}

QTEST_APPLESS_MAIN(TestAuthProtocol)
#include "tst_auth_protocol.moc"
