package io.safeaudit.core.processing;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.exception.AuditProcessingException;
import io.safeaudit.core.spi.AuditEventProcessor;
import io.safeaudit.core.spi.AuditSink;
import io.safeaudit.core.spi.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Abstract processing pipeline for audit events.
 * Applies a chain of processors before storing events.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public abstract class AuditProcessingPipeline {

    private static final Logger log = LoggerFactory.getLogger(AuditProcessingPipeline.class);

    protected final List<AuditEventProcessor> processors;
    protected final AuditStorage storage;
    protected final Optional<AuditSink> externalSink;

    protected AuditProcessingPipeline(
            List<AuditEventProcessor> processors,
            AuditStorage storage,
            Optional<AuditSink> externalSink) {

        this.processors = processors.stream()
                .sorted(Comparator.comparingInt(AuditEventProcessor::getOrder))
                .toList();
        this.storage = storage;
        this.externalSink = externalSink;
    }

    /**
     * Process an audit event through the pipeline.
     */
    public void process(AuditEvent rawEvent) {
        try {
            onBeforeProcessing(rawEvent);

            AuditEvent processedEvent = applyProcessors(rawEvent);

            persistEvent(processedEvent);

            forwardToExternalSink(processedEvent);

            onAfterProcessing(processedEvent);

        } catch (Exception e) {
            handleProcessingError(rawEvent, e);
        }
    }

    /**
     * Apply all processors in order.
     */
    protected AuditEvent applyProcessors(AuditEvent event) {
        AuditEvent current = event;
        for (var processor : processors) {
            try {
                current = processor.process(current);
            } catch (Exception e) {
                log.error("Processor {} failed: {}", processor.getClass().getSimpleName(), e.getMessage());
                // Continue with other processors
            }
        }
        return current;
    }

    /**
     * Persist event to storage with retry logic.
     */
    protected void persistEvent(AuditEvent event) {
        int maxAttempts = 3;
        int attempt = 0;

        while (true) {
            try {
                storage.store(event);
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new AuditProcessingException(
                            "Failed to persist event after " + maxAttempts + " attempts",
                            event,
                            e
                    );
                }
                backoff(attempt);
            }
        }
    }

    /**
     * Forward event to external sink (best-effort).
     */
    protected void forwardToExternalSink(AuditEvent event) {
        externalSink.ifPresent(sink -> {
            try {
                sink.send(event);
            } catch (Exception e) {
                log.warn("Failed to send event to external sink: {}", e.getMessage());
                // Don't fail the main pipeline
            }
        });
    }

    /**
     * Exponential backoff for retries.
     */
    protected void backoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void onBeforeProcessing(AuditEvent event);

    protected abstract void onAfterProcessing(AuditEvent event);

    protected abstract void handleProcessingError(AuditEvent event, Exception e);
}
