package io.safeaudit.core.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Nelson Tanko
 */
public class UUIDv7Generator implements AuditEventIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        return generateUUIDv7().toString();
    }

    private UUID generateUUIDv7() {
        // Get current timestamp in milliseconds
        long timestamp = Instant.now().toEpochMilli();

        // UUIDv7 format:
        // unix_ts_ms (48 bits) | ver (4 bits) | rand_a (12 bits) |
        // var (2 bits) | rand_b (62 bits)

        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long mostSigBits = (timestamp << 16) | (0x7000L) | (randomBytes[0] & 0x0FFF);
        long leastSigBits = (0x8000000000000000L | ((long) randomBytes[1] << 56)) |
                ((long) randomBytes[2] << 48) |
                ((long) randomBytes[3] << 40) |
                ((long) randomBytes[4] << 32) |
                ((long) randomBytes[5] << 24) |
                ((long) randomBytes[6] << 16) |
                ((long) randomBytes[7] << 8) |
                randomBytes[8];

        return new UUID(mostSigBits, leastSigBits);
    }
}
