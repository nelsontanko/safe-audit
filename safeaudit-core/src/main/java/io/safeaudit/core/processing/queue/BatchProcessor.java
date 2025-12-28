package io.safeaudit.core.processing.queue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Batches audit events for efficient bulk insertion.
 *
 * @author Nelson Tanko
 */
public class BatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(BatchProcessor.class);

    private final AuditStorage storage;
    private final int batchSize;
    private final Duration batchTimeout;
    private final List<AuditEvent> currentBatch;
    private final Counter persistedCounter;
    private Instant lastFlush;

    public BatchProcessor(
            AuditStorage storage,
            int batchSize,
            Duration batchTimeout,
            MeterRegistry meterRegistry) {

        this.storage = storage;
        this.batchSize = batchSize;
        this.batchTimeout = batchTimeout;
        this.currentBatch = new ArrayList<>(batchSize);
        this.lastFlush = Instant.now();

        this.persistedCounter = Counter.builder("audit.events.persisted")
                .description("Number of events persisted")
                .register(meterRegistry);
    }

    public synchronized void add(AuditEvent event) {
        currentBatch.add(event);

        if (shouldFlush()) {
            flush();
        }
    }

    private boolean shouldFlush() {
        return currentBatch.size() >= batchSize ||
                Duration.between(lastFlush, Instant.now()).compareTo(batchTimeout) > 0;
    }

    public synchronized void flush() {
        if (currentBatch.isEmpty()) {
            return;
        }

        try {
            int persisted = storage.storeBatch(List.copyOf(currentBatch));
            persistedCounter.increment(persisted);
            log.debug("Persisted batch of {} events", persisted);
        } catch (Exception e) {
            log.error("Failed to persist batch of {} events", currentBatch.size(), e);
            // Implement fallback: write to file, dead letter queue, etc.
        } finally {
            currentBatch.clear();
            lastFlush = Instant.now();
        }
    }
}