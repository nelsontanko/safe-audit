package io.safeaudit.web.capture;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.spi.AuditEventCapture;

/**
 * Default implementation of AuditEventCapture.
 * Delegates to the configured processing pipeline.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class DefaultAuditEventCapture implements AuditEventCapture {

    private final AuditProcessingPipeline pipeline;

    public DefaultAuditEventCapture(AuditProcessingPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void capture(AuditEvent event) {
        pipeline.process(event);
    }
}