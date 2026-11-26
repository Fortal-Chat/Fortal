#include <QTest>

#include "ApiError.h"

class TestApiError : public QObject {
    Q_OBJECT

private slots:
    void fromBodyValid();
    void fromBodyWithoutMessage();
    void fromBodyFallback_data();
    void fromBodyFallback();
};

void TestApiError::fromBodyValid() {
    const auto error = ApiError::fromBody(R"({"error":{"code":"username_taken","message":"Имя занято"}})");
    QCOMPARE(error.code, QString("username_taken"));
    QCOMPARE(error.message, QString("Имя занято"));
}

void TestApiError::fromBodyWithoutMessage() {
    const auto error = ApiError::fromBody(R"({"error":{"code":"invalid_credentials"}})");
    QCOMPARE(error.code, QString("invalid_credentials"));
    QVERIFY(error.message.isEmpty());
}

void TestApiError::fromBodyFallback_data() {
    QTest::addColumn<QByteArray>("body");

    QTest::newRow("empty") << QByteArray("");
    QTest::newRow("html") << QByteArray("<html><body>502 Bad Gateway</body></html>");
    QTest::newRow("array") << QByteArray("[1,2]");
    QTest::newRow("no error") << QByteArray(R"({"status":"ok"})");
    QTest::newRow("spring default") << QByteArray(R"({"timestamp":"2026-11-26T10:00:00Z","status":404,"error":"Not Found","path":"/x"})");
    QTest::newRow("empty code") << QByteArray(R"({"error":{"code":"","message":"x"}})");
}

void TestApiError::fromBodyFallback() {
    QFETCH(QByteArray, body);

    const auto error = ApiError::fromBody(body);
    QCOMPARE(error.code, QString("bad_response"));
    QVERIFY(!error.message.isEmpty());
}

QTEST_APPLESS_MAIN(TestApiError)
#include "tst_api_error.moc"
