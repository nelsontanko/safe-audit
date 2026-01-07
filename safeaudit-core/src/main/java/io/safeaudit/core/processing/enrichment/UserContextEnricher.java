package io.safeaudit.core.processing.enrichment;

import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class UserContextEnricher implements AuditEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(UserContextEnricher.class);
    private static final boolean SPRING_SECURITY_PRESENT = ClassUtils.isPresent(
            "org.springframework.security.core.context.SecurityContextHolder",
            UserContextEnricher.class.getClassLoader()
    );

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
        if (!SPRING_SECURITY_PRESENT) {
            return null;
        }
        try {
            return SecurityAccess.getUserId();
        } catch (Throwable t) {
            log.trace("Failed to extract user ID from Spring Security", t);
            return null;
        }
    }

    private String getUsernameFromSecurity() {
        if (!SPRING_SECURITY_PRESENT) {
            return null;
        }
        try {
            return SecurityAccess.getUsername();
        } catch (Throwable t) {
            log.trace("Failed to extract username from Spring Security", t);
            return null;
        }
    }

    /**
     * This class will only be loaded if SPRING_SECURITY_PRESENT is true
     * and one of its methods is called.
     */
    private static class SecurityAccess {
        static String getUserId() {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
            return null;
        }

        static String getUsername() {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof String username) {
                    return username;
                }
                return principal.toString();
            }
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 100; // Run early
    }
}
