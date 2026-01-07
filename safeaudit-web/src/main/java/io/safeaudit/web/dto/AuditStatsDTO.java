package io.safeaudit.web.dto;

import java.time.Instant;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public record AuditStatsDTO(
        long totalEvents,
        long infoCount,
        long warnCount,
        long criticalCount,
        Instant from,
        Instant to
) {
}