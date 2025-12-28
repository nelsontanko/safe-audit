package io.safeaudit.core.spi;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public record IntegrityReport(
        boolean valid, Instant from, Instant to,
        long totalEvents, long verifiedEvents,
        List<IntegrityViolation> violations
) {

    public IntegrityReport(
            boolean valid,
            Instant from,
            Instant to,
            long totalEvents,
            long verifiedEvents,
            List<IntegrityViolation> violations) {

        this.valid = valid;
        this.from = from;
        this.to = to;
        this.totalEvents = totalEvents;
        this.verifiedEvents = verifiedEvents;
        this.violations = violations != null ?
                Collections.unmodifiableList(violations) :
                Collections.emptyList();
    }

    public record IntegrityViolation(String eventId, String description, Instant timestamp) {
    }
}