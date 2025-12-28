package io.safeaudit.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe sequence number generator for audit events.
 * Each application instance maintains its own sequence.
 *
 * @author Nelson Tanko
 */
public class SequenceNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(0);

    /**
     * Get next sequence number.
     */
    public long next() {
        return sequence.incrementAndGet();
    }

    /**
     * Get current sequence number without incrementing.
     */
    public long current() {
        return sequence.get();
    }

    /**
     * Reset sequence to specific value (for recovery scenarios).
     */
    public void reset(long value) {
        sequence.set(value);
    }
}
