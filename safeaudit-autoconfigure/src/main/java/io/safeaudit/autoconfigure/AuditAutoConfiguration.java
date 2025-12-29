package io.safeaudit.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.util.AuditEventIdGenerator;
import io.safeaudit.core.util.AuditMetrics;
import io.safeaudit.core.util.SequenceNumberGenerator;
import io.safeaudit.core.util.UUIDv7Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author Nelson Tanko
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "audit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties.class)
@Import({
        AuditCaptureAutoConfiguration.class,
        AuditProcessingAutoConfiguration.class,
        AuditStorageAutoConfiguration.class,
        AuditReportingAutoConfiguration.class,
        AuditComplianceAutoConfiguration.class
})
public class AuditAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditAutoConfiguration.class);

    /**
     * Event ID generator (UUIDv7 for time-ordered IDs).
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditEventIdGenerator auditEventIdGenerator() {
        return new UUIDv7Generator();
    }

    /**
     * Sequence number generator for events.
     */
    @Bean
    @ConditionalOnMissingBean
    public SequenceNumberGenerator sequenceNumberGenerator() {
        return new SequenceNumberGenerator();
    }

    /**
     * Metrics registry for audit framework.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    public AuditMetrics auditMetrics(MeterRegistry meterRegistry) {
        return new AuditMetrics(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    /**
     * Application startup runner to initialize audit framework.
     */
    @Bean
    public ApplicationRunner auditFrameworkInitializer(
            AuditProperties properties,
            AuditStorage storage,
            ApplicationContext applicationContext) {

        return args -> {
            log.info("═══════════════════════════════════════════════════");
            log.info("  SafeAudit Framework Initialization");
            log.info("═══════════════════════════════════════════════════");
            log.info("Version: {}", getFrameworkVersion());
            log.info("Application: {}", applicationContext.getEnvironment().getProperty("spring.application.name"));

            var health = storage.checkHealth();
            if (!health.isHealthy()) {
                log.error("❌ Audit storage health check failed: {}", health.getMessage());
                if (shouldFailOnStartup(properties)) {
                    throw new IllegalStateException("Audit storage is not healthy");
                }
            } else {
                log.info("✓ Storage: Healthy");
            }

            log.info("✓ Capture: HTTP={}, Method={}",
                    properties.getCapture().getHttp().isEnabled(),
                    properties.getCapture().getMethod().isEnabled());
            log.info("✓ Processing: Mode={}", properties.getProcessing().getMode());
            log.info("✓ Reporting: API={}, UI={}",
                    properties.getReporting().getApi().isEnabled(),
                    properties.getReporting().getUi().isEnabled());
            log.info("✓ Compliance: Regulations={}", properties.getProcessing().getCompliance().getRegulations());

            log.info("═══════════════════════════════════════════════════");
            log.info("  SafeAudit Framework Ready");
            log.info("═══════════════════════════════════════════════════");
        };
    }

    private String getFrameworkVersion() {
        Package pkg = getClass().getPackage();
        return pkg != null && pkg.getImplementationVersion() != null ?
                pkg.getImplementationVersion() : "dev";
    }

    private boolean shouldFailOnStartup(AuditProperties properties) {
        // Could add a property for this
        return false; // Default: don't fail startup
    }
}
