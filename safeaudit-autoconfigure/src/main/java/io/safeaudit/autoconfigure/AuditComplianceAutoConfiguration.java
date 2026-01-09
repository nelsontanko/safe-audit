package io.safeaudit.autoconfigure;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.processing.compliance.CBNComplianceProfile;
import io.safeaudit.core.processing.compliance.ComplianceProcessor;
import io.safeaudit.core.processing.compliance.ComplianceProfile;
import io.safeaudit.core.processing.compliance.NDPAComplianceProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@AutoConfiguration
public class AuditComplianceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditComplianceAutoConfiguration.class);

    /**
     * Compliance processor.
     */
    @Bean
    @ConditionalOnMissingBean
    public ComplianceProcessor complianceProcessor(
            List<ComplianceProfile> profiles,
            AuditProperties properties) {
        return new ComplianceProcessor(profiles, properties);
    }


    /**
     * CBN (Central Bank of Nigeria) compliance profile.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "audit.processing.compliance",
            name = "regulations",
            havingValue = "CBN"
    )
    @ConditionalOnMissingBean(name = "cbnComplianceProfile")
    public ComplianceProfile cbnComplianceProfile() {
        log.info("Activating CBN compliance profile");
        return new CBNComplianceProfile();
    }

    /**
     * NDPA (Nigeria Data Protection Act) compliance profile.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "audit.processing.compliance",
            name = "regulations",
            havingValue = "NDPA"
    )
    @ConditionalOnMissingBean(name = "ndpaComplianceProfile")
    public ComplianceProfile ndpaComplianceProfile() {
        log.info("Activating NDPA compliance profile");
        return new NDPAComplianceProfile();
    }

    /**
     * Auto-configure compliance profiles based on configuration.
     */
    @Bean
    public ComplianceProfileInitializer complianceProfileInitializer(
            AuditProperties properties) {

        return new ComplianceProfileInitializer(properties);
    }

    /**
     * Compliance profile initializer that logs active regulations.
     */
    public record ComplianceProfileInitializer(AuditProperties properties) {
        public ComplianceProfileInitializer(AuditProperties properties) {
            this.properties = properties;
            Set<String> regulations = properties.getProcessing().getCompliance().getRegulations();

            if (regulations.isEmpty()) {
                log.info("No regulatory compliance profiles activated");
            } else {
                log.info("Active regulatory compliance profiles: {}", regulations);
            }
        }
    }
}

