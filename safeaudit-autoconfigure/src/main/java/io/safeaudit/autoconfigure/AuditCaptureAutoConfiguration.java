package io.safeaudit.autoconfigure;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditEventIdGenerator;
import io.safeaudit.core.util.SequenceNumberGenerator;
import io.safeaudit.web.capture.AuditAnnotationHandlerInterceptor;
import io.safeaudit.web.capture.AuditHttpFilter;
import io.safeaudit.web.capture.AuditMethodInterceptor;
import io.safeaudit.web.capture.DefaultAuditEventCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(name = "jakarta.servlet.Filter")
public class AuditCaptureAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditCaptureAutoConfiguration.class);

    /**
     * Default event capture implementation.
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditEventCapture auditEventCapture(AuditProcessingPipeline pipeline) {
        return new DefaultAuditEventCapture(pipeline);
    }

    /**
     * HTTP filter for request/response capture.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(
            prefix = "audit.capture.http",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<AuditHttpFilter> auditHttpFilter(
            AuditEventCapture eventCapture,
            AuditProperties properties,
            AuditEventIdGenerator idGenerator,
            ApplicationContext applicationContext,
            SequenceNumberGenerator sequenceNumberGenerator) {

        log.info("Registering HTTP audit filter");

        var filter = new AuditHttpFilter(
                eventCapture,
                properties,
                idGenerator,
                applicationContext,
                sequenceNumberGenerator
        );

        var registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");

        return registration;
    }

    /**
     * AOP interceptor for @Audited annotation.
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnProperty(
            prefix = "audit.capture.method",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditMethodInterceptor auditMethodInterceptor(
            AuditEventCapture eventCapture,
            AuditEventIdGenerator idGenerator,
            ApplicationContext applicationContext,
            SequenceNumberGenerator sequenceNumberGenerator) {

        log.info("Registering @Audited method interceptor");
        return new AuditMethodInterceptor(eventCapture, idGenerator, applicationContext, sequenceNumberGenerator);
    }

    /**
     * Registers the annotation handler interceptor.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(
            prefix = "audit.capture.http",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public WebMvcConfigurer auditAnnotationInterceptorConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(new AuditAnnotationHandlerInterceptor());
            }
        };
    }
}
