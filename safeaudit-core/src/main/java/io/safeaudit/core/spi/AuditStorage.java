package io.safeaudit.core.spi;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.HealthStatus;
import io.safeaudit.core.domain.IntegrityReport;
import io.safeaudit.core.domain.QueryCriteria;
import io.safeaudit.core.exception.AuditStorageException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for audit events.
 * Implementations must be thread-safe and support concurrent access.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public interface AuditStorage {

    /**
     * Persist a single audit event.
     * Must be idempotent based on eventId.
     *
     * @param event the audit event to store
     * @return true if persisted, false if duplicate
     * @throws AuditStorageException on unrecoverable errors
     */
    boolean store(AuditEvent event);

    /**
     * Persist multiple audit events in a batch.
     * Implementations should use batch operations for efficiency.
     *
     * @param events the list of events to store
     * @return number of events successfully persisted
     * @throws AuditStorageException on unrecoverable errors
     */
    int storeBatch(List<AuditEvent> events);

    /**
     * Retrieve a single audit event by ID.
     *
     * @param eventId the event identifier
     * @return the audit event if found
     */
    Optional<AuditEvent> findById(String eventId);

    /**
     * Query audit events based on criteria.
     *
     * @param criteria the query criteria
     * @return list of matching audit events
     */
    List<AuditEvent> query(QueryCriteria criteria);

    /**
     * Count audit events matching criteria.
     *
     * @param criteria the query criteria
     * @return count of matching events
     */
    long count(QueryCriteria criteria);

    /**
     * Verify integrity of event chain between time range.
     *
     * @param from start timestamp
     * @param to   end timestamp
     * @return integrity verification report
     */
    IntegrityReport verifyIntegrity(Instant from, Instant to);

    /**
     * Initialize storage schema if needed.
     * Called during application startup if auto-schema is enabled.
     */
    void initializeSchema();

    /**
     * Check storage health status.
     *
     * @return health status
     */
    HealthStatus checkHealth();
}