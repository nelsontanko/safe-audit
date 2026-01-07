package io.safeaudit.web.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditEventIdGenerator;
import io.safeaudit.core.util.ApplicationInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.time.Instant;

import static io.safeaudit.core.domain.enums.AuditSeverity.CRITICAL;

/**
 * AOP interceptor for @Audited annotation.
 * Captures method execution details
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
@Aspect
public class AuditMethodInterceptor implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuditMethodInterceptor.class);

    private final AuditEventCapture eventCapture;
    private final AuditEventIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final String applicationName;
    private final String applicationInstance;

    public AuditMethodInterceptor(
            AuditEventCapture eventCapture,
            AuditEventIdGenerator idGenerator,
            ApplicationContext applicationContext) {
        this.eventCapture = eventCapture;
        this.idGenerator = idGenerator;
        this.objectMapper = new ObjectMapper();
        this.applicationName = ApplicationInfo.getApplicationName(applicationContext);
        this.applicationInstance = ApplicationInfo.getApplicationInstance();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var startTime = Instant.now();
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            try {
                captureMethodAudit(signature, joinPoint.getArgs(), result, exception, audited, startTime);
            } catch (Exception e) {
                log.error("Failed to capture method audit", e);
            }
        }
    }

    private void captureMethodAudit(
            MethodSignature signature,
            Object[] args,
            Object result,
            Throwable exception,
            Audited audited,
            Instant startTime) {

        var eventType = audited.eventType().isBlank() ?
                signature.getMethod().getName().toUpperCase() :
                audited.eventType();

        var resource = audited.resource().isBlank() ?
                signature.getDeclaringTypeName() :
                audited.resource();

        var requestPayload = audited.includeArgs() ?
                serializeArgs(args) : null;

        var responsePayload = audited.includeResult() && result != null ?
                serializeResult(result) : null;

        var event = AuditEvent.builder()
                .eventId(idGenerator.generate())
                .timestamp(startTime)
                .eventType(eventType)
                .severity(exception != null ? CRITICAL : audited.severity())
                .resource(resource)
                .action(signature.getName())
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .capturedBy(ApplicationInfo.getFrameworkVersion())
                .applicationName(applicationName)
                .applicationInstance(applicationInstance)
                .build();

        eventCapture.capture(event);
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "[Serialization failed: " + e.getMessage() + "]";
        }
    }

    private String serializeResult(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "[Serialization failed: " + e.getMessage() + "]";
        }
    }
}