#pragma once

class Backoff {
public:
    explicit Backoff(int minMs = 1000, int maxMs = 30000);

    int next();
    void reset();

private:
    int m_minMs;
    int m_maxMs;
    int m_currentMs;
};
