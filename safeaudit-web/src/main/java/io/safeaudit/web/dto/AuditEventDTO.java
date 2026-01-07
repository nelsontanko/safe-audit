package io.safeaudit.web.dto;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;

import java.time.Instant;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public record AuditEventDTO(
        String eventId,
        long sequenceNumber,
        Instant timestamp,
        String eventType,
        AuditSeverity severity,
        String userId,
        String username,
        String ipAddress,
        String resource,
        String action,
        String sessionId,
        Integer httpStatusCode,
        String applicationName
) {
    public static AuditEventDTO from(AuditEvent event) {
        return new AuditEventDTO(
                event.eventId(),
                event.sequenceNumber(),
                event.timestamp(),
                event.eventType(),
                event.severity(),
                event.userId(),
                event.username(),
                event.ipAddress(),
                event.resource(),
                event.action(),
                event.sessionId(),
                event.httpStatusCode(),
                event.applicationName()
        );
    }
}
