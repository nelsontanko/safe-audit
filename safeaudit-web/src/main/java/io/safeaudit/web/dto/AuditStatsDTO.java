package io.safeaudit.web.dto;

import java.time.Instant;

/**
 * @author Nelson Tanko
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