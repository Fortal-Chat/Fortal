#include "Backoff.h"

#include <algorithm>

Backoff::Backoff(int minMs, int maxMs) : m_minMs(minMs), m_maxMs(maxMs), m_currentMs(minMs) {}

int Backoff::next() {
    const int delay = m_currentMs;
    m_currentMs = std::min(m_currentMs * 2, m_maxMs);
    return delay;
}

void Backoff::reset() {
    m_currentMs = m_minMs;
}
