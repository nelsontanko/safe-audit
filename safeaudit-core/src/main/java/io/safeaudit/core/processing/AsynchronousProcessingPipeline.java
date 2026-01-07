package io.safeaudit.core.processing;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;
import io.safeaudit.core.spi.AuditSink;
import io.safeaudit.core.spi.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Asynchronous processing pipeline - events are queued and processed by workers.
 * Minimal request latency but eventual consistency.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AsynchronousProcessingPipeline extends AuditProcessingPipeline {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousProcessingPipeline.class);

    private final BlockingQueue<AuditEvent> queue;
    private final Timer processingTimer;

    public AsynchronousProcessingPipeline(
            List<AuditEventProcessor> processors,
            AuditStorage storage,
            Optional<AuditSink> externalSink,
            BlockingQueue<AuditEvent> queue,
            MeterRegistry meterRegistry) {
        super(processors, storage, externalSink);

        this.queue = queue;
        this.processingTimer = Timer.builder("audit.processing.time")
                .tag("mode", "async")
                .description("Time to process audit event asynchronously")
                .register(meterRegistry);
    }

    @Override
    protected void onBeforeProcessing(AuditEvent event) {
        log.trace("Processing event from queue: {}", event.eventId());
    }

    @Override
    protected void onAfterProcessing(AuditEvent event) {
        log.trace("Event processed from queue: {}", event.eventId());
    }

    @Override
    protected void handleProcessingError(AuditEvent event, Exception e) {
        log.error("Failed to process queued event {}: {}", event.eventId(), e.getMessage());
        // Could implement dead letter queue here
    }

    @Override
    public void process(AuditEvent rawEvent) {
        processingTimer.record(() -> super.process(rawEvent));
    }

    /**
     * Queue an event for async processing.
     */
    public boolean enqueue(AuditEvent event) {
        return queue.offer(event);
    }

    public int getQueueSize() {
        return queue.size();
    }
}