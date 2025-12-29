package io.safeaudit.web.capture;

import io.safeaudit.core.domain.enums.AuditSeverity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method-level audit capture.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * Event type identifier.
     */
    String eventType() default "";

    /**
     * Event severity level.
     */
    AuditSeverity severity() default AuditSeverity.INFO;

    /**
     * Include method arguments in audit payload.
     */
    boolean includeArgs() default false;

    /**
     * Include method return value in audit payload.
     */
    boolean includeResult() default false;

    /**
     * Resource identifier (defaults to class name).
     */
    String resource() default "";

    /**
     * Custom description for the audit event.
     */
    String description() default "";
}