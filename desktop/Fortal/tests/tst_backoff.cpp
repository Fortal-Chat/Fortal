#include <QTest>

#include "Backoff.h"

class TestBackoff : public QObject {
    Q_OBJECT

private slots:
    void doublesUntilMax();
    void resetStartsOver();
    void customLimits();
};

void TestBackoff::doublesUntilMax() {
    Backoff backoff;
    const QList<int> expected{1000, 2000, 4000, 8000, 16000, 30000, 30000};
    for (const int ms : expected) {
        QCOMPARE(backoff.next(), ms);
    }
}

void TestBackoff::resetStartsOver() {
    Backoff backoff;
    backoff.next();
    backoff.next();
    backoff.next();

    backoff.reset();

    QCOMPARE(backoff.next(), 1000);
    QCOMPARE(backoff.next(), 2000);
}

void TestBackoff::customLimits() {
    Backoff backoff(100, 300);
    QCOMPARE(backoff.next(), 100);
    QCOMPARE(backoff.next(), 200);
    QCOMPARE(backoff.next(), 300);
    QCOMPARE(backoff.next(), 300);
}

QTEST_APPLESS_MAIN(TestBackoff)
#include "tst_backoff.moc"
