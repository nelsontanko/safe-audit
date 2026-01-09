package io.safeaudit.core.processing;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.HealthStatus;
import io.safeaudit.core.domain.IntegrityReport;
import io.safeaudit.core.domain.QueryCriteria;
import io.safeaudit.core.spi.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fallback implementation of AuditStorage that logs events to the console.
 * Used when no other storage implementation is available.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class LoggingAuditStorage implements AuditStorage {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuditStorage.class);

    @Override
    public boolean store(AuditEvent event) {
        log.info("AUDIT: {}", event);
        return true;
    }

    @Override
    public int storeBatch(List<AuditEvent> events) {
        if (events != null) {
            events.forEach(this::store);
            return events.size();
        }
        return 0;
    }

    @Override
    public Optional<AuditEvent> findById(String eventId) {
        return Optional.empty();
    }

    @Override
    public List<AuditEvent> query(QueryCriteria criteria) {
        return Collections.emptyList();
    }

    @Override
    public long count(QueryCriteria criteria) {
        return 0;
    }

    @Override
    public IntegrityReport verifyIntegrity(Instant from, Instant to) {
        return null; // Or a no-op report
    }

    @Override
    public void initializeSchema() {
        log.info("Logging storage initialized (no schema needed)");
    }

    @Override
    public HealthStatus checkHealth() {
        return HealthStatus.healthy("LoggingAuditStorage");
    }
}
