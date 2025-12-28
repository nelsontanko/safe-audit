package io.safeaudit.core.processing.enrichment;

import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;

import java.util.UUID;

/**
 * Enriches audit events with correlation ID for distributed tracing.
 *
 * @author Nelson Tanko
 */
public class CorrelationIdEnricher implements AuditEventProcessor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public AuditEvent process(AuditEvent event) {
        if (event.sessionId() != null) {
            return event;
        }

        var correlationId = AuditContext.get(AuditContext.CORRELATION_ID, String.class)
                .orElseGet(() -> UUID.randomUUID().toString());

        return AuditEvent.builder()
                .eventId(event.eventId())
                .sequenceNumber(event.sequenceNumber())
                .timestamp(event.timestamp())
                .eventType(event.eventType())
                .severity(event.severity())
                .userId(event.userId())
                .username(event.username())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .resource(event.resource())
                .action(event.action())
                .sessionId(correlationId)
                .tenantId(event.tenantId())
                .requestPayload(event.requestPayload())
                .responsePayload(event.responsePayload())
                .httpStatusCode(event.httpStatusCode())
                .compliance(event.compliance())
                .previousEventHash(event.previousEventHash())
                .eventHash(event.eventHash())
                .capturedBy(event.capturedBy())
                .applicationName(event.applicationName())
                .applicationInstance(event.applicationInstance())
                .build();
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
