package io.safeaudit.web.capture;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditEventIdGenerator;
import io.safeaudit.core.util.ApplicationInfo;
import io.safeaudit.core.util.IPAddressExtractor;
import io.safeaudit.core.util.PayloadExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AuditHttpFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuditHttpFilter.class);

    private final AuditEventCapture eventCapture;
    private final AuditProperties properties;
    private final AuditEventIdGenerator idGenerator;
    private final String applicationName;
    private final String applicationInstance;

    public AuditHttpFilter(
            AuditEventCapture eventCapture,
            AuditProperties properties,
            AuditEventIdGenerator idGenerator,
            ApplicationContext applicationContext) {
        this.eventCapture = eventCapture;
        this.properties = properties;
        this.idGenerator = idGenerator;
        this.applicationName = ApplicationInfo.getApplicationName(applicationContext);
        this.applicationInstance = ApplicationInfo.getApplicationInstance();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!shouldAudit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request/response to capture content
        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        var startTime = Instant.now();
        var correlationId = extractOrGenerateCorrelationId(request);

        AuditContext.set(AuditContext.CORRELATION_ID, correlationId);
        AuditContext.set(AuditContext.REQUEST_URI, request.getRequestURI());
        AuditContext.set(AuditContext.HTTP_METHOD, request.getMethod());

        Throwable exception = null;

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception t) {
            exception = t;
            throw t;
        } finally {
            try {
                captureAuditEvent(wrappedRequest, wrappedResponse, startTime, exception);
            } catch (Exception e) {
                // CRITICAL: Never fail the request due to audit errors
                log.error("Failed to capture audit event", e);
            } finally {
                wrappedResponse.copyBodyToResponse();
                AuditContext.clear();
            }
        }
    }

    private void captureAuditEvent(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            Instant startTime,
            Throwable exception) {

        Object shouldAudit = request.getAttribute(AuditAnnotationHandlerInterceptor.SHOULD_AUDIT_ATTRIBUTE);
        if (!Boolean.TRUE.equals(shouldAudit)) {
            return;
        }

        var config = properties.getCapture().getHttp();

        var requestPayload = config.isIncludeRequestBody() ?
                PayloadExtractor.extractRequestBody(request, config.getMaxBodySize()) : null;

        var responsePayload = config.isIncludeResponseBody() ?
                PayloadExtractor.extractResponseBody(response, config.getMaxBodySize()) : null;

        var event = AuditEvent.builder()
                .eventId(idGenerator.generate())
                .timestamp(startTime)
                .eventType(determineEventType(request, response))
                .severity(determineSeverity(response.getStatus(), exception))
                .ipAddress(IPAddressExtractor.extract(request))
                .userAgent(request.getHeader("User-Agent"))
                .resource(request.getRequestURI())
                .action(request.getMethod())
                .sessionId(request.getSession(false) != null ?
                        request.getSession(false).getId() : null)
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .httpStatusCode(response.getStatus())
                .capturedBy(ApplicationInfo.getFrameworkVersion())
                .applicationName(applicationName)
                .applicationInstance(applicationInstance)
                .build();

        eventCapture.capture(event);
    }

    private boolean shouldAudit(HttpServletRequest request) {
        var uri = request.getRequestURI();

        if (uri.startsWith("/actuator")) {
            return false;
        }

        // Skip static resources
        if (uri.matches(".+\\.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$")) {
            return false;
        }

        for (String pattern : properties.getCapture().getHttp().getExclusionPatterns()) {
            if (uri.matches(pattern)) {
                return false;
            }
        }

        return true;
    }

    private String determineEventType(HttpServletRequest request, HttpServletResponse response) {
        var method = request.getMethod();
        int status = response.getStatus();

        if (status >= 400) {
            return "HTTP_ERROR";
        }

        return switch (method) {
            case "POST" -> "HTTP_CREATE";
            case "PUT", "PATCH" -> "HTTP_UPDATE";
            case "DELETE" -> "HTTP_DELETE";
            default -> "HTTP_ACCESS";
        };
    }

    private AuditSeverity determineSeverity(int status, Throwable exception) {
        if (exception != null || status >= 500) {
            return AuditSeverity.CRITICAL;
        }
        if (status >= 400) {
            return AuditSeverity.WARN;
        }
        return AuditSeverity.INFO;
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        var correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = request.getHeader("X-Request-ID");
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
