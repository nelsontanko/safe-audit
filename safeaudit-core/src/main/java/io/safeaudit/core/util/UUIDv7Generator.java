package io.safeaudit.core.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Nelson Tanko
 */
public class UUIDv7Generator implements AuditEventIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final AtomicLong lastTimestamp = new AtomicLong(0);
    private final AtomicLong counter = new AtomicLong(0);
    private static final long COUNTER_MAX = 0xFFFL; // 12 bits max (4095)

    @Override
    public String generate() {
        return generateUUIDv7().toString();
    }

    private UUID generateUUIDv7() {
        long timestamp = Instant.now().toEpochMilli();
        long previousTimestamp;
        long currentCounter;

        synchronized (this) {
            previousTimestamp = lastTimestamp.get();

            if (timestamp == previousTimestamp) {
                // Same millisecond, increment counter
                currentCounter = counter.incrementAndGet();
                if (currentCounter > COUNTER_MAX) {
                    // Counter overflow, wait for next millisecond
                    while (timestamp <= previousTimestamp) {
                        timestamp = Instant.now().toEpochMilli();
                    }
                    counter.set(0);
                    currentCounter = 0;
                }
            } else {
                if (timestamp < previousTimestamp) {
                    // Clock moved backward, handle gracefully
                    timestamp = previousTimestamp + 1;
                }
                counter.set(0);
                currentCounter = 0;
            }

            lastTimestamp.set(timestamp);
        }

        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        // UUIDv7 format:
        // unix_ts_ms (48 bits) | ver (4 bits) = 0x7 | rand_a (12 bits) |
        // var (2 bits) | rand_b (62 bits)

        // Most significant bits (timestamp + version + counter)
        long mostSigBits = (timestamp << 16) |  // 48 bits timestamp
                (0x7000L) |                     // Version 7 (4 bits: 0b0111)
                (currentCounter & 0x0FFFL);     // 12 bits counter (0-4095)

        // Least significant bits (random)
        long leastSigBits = getLeastSigBits(randomBytes);

        return new UUID(mostSigBits, leastSigBits);
    }

    private long getLeastSigBits(byte[] randomBytes) {
        long leastSigBits = ((randomBytes[0] & 0xFFL) << 56) |
                ((randomBytes[1] & 0xFFL) << 48) |
                ((randomBytes[2] & 0xFFL) << 40) |
                ((randomBytes[3] & 0xFFL) << 32) |
                ((randomBytes[4] & 0xFFL) << 24) |
                ((randomBytes[5] & 0xFFL) << 16) |
                ((randomBytes[6] & 0xFFL) << 8) |
                (randomBytes[7] & 0xFFL);

        // Set variant to RFC 4122 (2 bits: 10)
        leastSigBits &= 0x3FFFFFFFFFFFFFFFL; // Clear variant bits
        leastSigBits |= 0x8000000000000000L; // Set to RFC 4122 variant
        return leastSigBits;
    }
}