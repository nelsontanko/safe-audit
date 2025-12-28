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

/**
 * Synchronous processing pipeline - processes events immediately.
 * Increases request latency but guarantees immediate persistence.
 *
 * @author Nelson Tanko
 */
public class SynchronousProcessingPipeline extends AuditProcessingPipeline {

    private static final Logger log = LoggerFactory.getLogger(SynchronousProcessingPipeline.class);

    private final Timer processingTimer;

    public SynchronousProcessingPipeline(
            List<AuditEventProcessor> processors,
            AuditStorage storage,
            Optional<AuditSink> externalSink,
            MeterRegistry meterRegistry) {
        super(processors, storage, externalSink);

        this.processingTimer = Timer.builder("audit.processing.time")
                .tag("mode", "sync")
                .description("Time to process audit event synchronously")
                .register(meterRegistry);
    }

    @Override
    protected void onBeforeProcessing(AuditEvent event) {
        log.debug("Processing event synchronously: {}", event.eventId());
    }

    @Override
    protected void onAfterProcessing(AuditEvent event) {
        log.debug("Event processed successfully: {}", event.eventId());
    }

    @Override
    protected void handleProcessingError(AuditEvent event, Exception e) {
        log.error("Failed to process event {}: {}", event.eventId(), e.getMessage(), e);
        // In sync mode, we can't recover - log and continue
    }

    @Override
    public void process(AuditEvent rawEvent) {
        processingTimer.record(() -> super.process(rawEvent));
    }
}