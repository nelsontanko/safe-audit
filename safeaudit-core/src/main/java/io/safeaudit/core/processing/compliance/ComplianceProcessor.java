package io.safeaudit.core.processing.compliance;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.exception.ComplianceViolationException;
import io.safeaudit.core.spi.AuditEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Processor that applies active compliance profiles to audit events.
 * Only profiles matching the configured regulations are applied.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class ComplianceProcessor implements AuditEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(ComplianceProcessor.class);

    private final List<ComplianceProfile> profiles;
    private final Set<String> activeRegulations;

    public ComplianceProcessor(List<ComplianceProfile> profiles, AuditProperties properties) {
        this.profiles = profiles;
        this.activeRegulations = properties.getProcessing().getCompliance().getRegulations();
    }

    @Override
    public AuditEvent process(AuditEvent event) {
        AuditEvent current = event;

        for (ComplianceProfile profile : profiles) {
            if (activeRegulations.contains(profile.getRegulationCode())) {
                try {
                    current = profile.enrich(current);

                    profile.validate(current);
                } catch (ComplianceViolationException e) {
                    log.warn("Compliance violation for regulation {}: {}", profile.getRegulationCode(), e.getMessage());
                } catch (Exception e) {
                    log.error("Error applying compliance profile {}", profile.getRegulationCode(), e);
                }
            }
        }
        return current;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
