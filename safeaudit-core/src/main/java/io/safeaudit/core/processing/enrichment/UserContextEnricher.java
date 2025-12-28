package io.safeaudit.core.processing.enrichment;

import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Nelson Tanko
 */
public class UserContextEnricher implements AuditEventProcessor {

    @Override
    public AuditEvent process(AuditEvent event) {
        // If already set, don't override
        if (event.userId() != null) {
            return event;
        }

        var userId = extractUserId();
        var username = extractUsername();

        if (userId == null && username == null) {
            return event;
        }

        return AuditEvent.builder()
                .eventId(event.eventId())
                .sequenceNumber(event.sequenceNumber())
                .timestamp(event.timestamp())
                .eventType(event.eventType())
                .severity(event.severity())
                .userId(userId)
                .username(username)
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .resource(event.resource())
                .action(event.action())
                .sessionId(event.sessionId())
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

    private String extractUserId() {
        return AuditContext.get(AuditContext.USER_ID, String.class)
                .orElseGet(this::getUserIdFromSecurity);
    }

    private String extractUsername() {
        return AuditContext.get(AuditContext.USERNAME, String.class)
                .orElseGet(this::getUsernameFromSecurity);
    }

    private String getUserIdFromSecurity() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception _) {
            // Spring Security not available or not configured
        }
        return null;
    }

    private String getUsernameFromSecurity() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof String username) {
                    return username;
                }
                return principal.toString();
            }
        } catch (Exception e) {
            // Spring Security not available
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 100; // Run early
    }
}
