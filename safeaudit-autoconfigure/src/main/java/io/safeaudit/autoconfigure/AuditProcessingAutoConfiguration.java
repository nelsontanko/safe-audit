package io.safeaudit.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.processing.AsynchronousProcessingPipeline;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.processing.SynchronousProcessingPipeline;
import io.safeaudit.core.processing.compliance.PIIMasker;
import io.safeaudit.core.processing.enrichment.CorrelationIdEnricher;
import io.safeaudit.core.processing.enrichment.UserContextEnricher;
import io.safeaudit.core.processing.integrity.HashCalculator;
import io.safeaudit.core.processing.queue.BackpressureHandler;
import io.safeaudit.core.processing.queue.VirtualThreadAuditQueue;
import io.safeaudit.core.spi.AuditEventProcessor;
import io.safeaudit.core.spi.AuditSink;
import io.safeaudit.core.spi.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Nelson Tanko
 */
@AutoConfiguration
@ConditionalOnBean(AuditStorage.class)
public class AuditProcessingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditProcessingAutoConfiguration.class);

    /**
     * User context enricher.
     */
    @Bean
    @ConditionalOnMissingBean(UserContextEnricher.class)
    @ConditionalOnProperty(prefix = "audit.processing.enrichment", name = "user-context", havingValue = "true", matchIfMissing = true)
    public UserContextEnricher userContextEnricher() {
        return new UserContextEnricher();
    }

    /**
     * Correlation ID enricher.
     */
    @Bean
    @ConditionalOnMissingBean(CorrelationIdEnricher.class)
    @ConditionalOnProperty(prefix = "audit.processing.enrichment", name = "correlation-id", havingValue = "true", matchIfMissing = true)
    public CorrelationIdEnricher correlationIdEnricher() {
        return new CorrelationIdEnricher();
    }

    /**
     * PII masker.
     */
    @Bean
    @ConditionalOnMissingBean(PIIMasker.class)
    @ConditionalOnProperty(prefix = "audit.processing.compliance.pii-masking", name = "enabled", havingValue = "true", matchIfMissing = true)
    public PIIMasker piiMasker(AuditProperties properties) {
        var config = properties.getProcessing().getCompliance().getPiiMasking();
        return new PIIMasker(config.getFields(), config.getStrategy());
    }

    /**
     * Hash calculator for integrity.
     */
    @Bean
    @ConditionalOnMissingBean(HashCalculator.class)
    @ConditionalOnProperty(prefix = "audit.integrity.hashing", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HashCalculator hashCalculator(AuditProperties properties) {
        var config = properties.getIntegrity().getHashing();
        return new HashCalculator(config.getAlgorithm(), config.isIncludePreviousHash());
    }

    /**
     * Synchronous processing pipeline.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.processing", name = "mode", havingValue = "SYNC")
    @ConditionalOnMissingBean(AuditProcessingPipeline.class)
    public SynchronousProcessingPipeline synchronousProcessingPipeline(
            List<AuditEventProcessor> processors,
            AuditStorage storage,
            Optional<AuditSink> externalSink,
            MeterRegistry meterRegistry) {

        log.info("Creating synchronous processing pipeline with {} processors", processors.size());
        return new SynchronousProcessingPipeline(processors, storage, externalSink, meterRegistry);
    }

    /**
     * Asynchronous processing pipeline.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.processing", name = "mode", havingValue = "ASYNC", matchIfMissing = true)
    @ConditionalOnMissingBean(AuditProcessingPipeline.class)
    public AsynchronousProcessingPipeline asynchronousProcessingPipeline(
            List<AuditEventProcessor> processors,
            AuditStorage storage,
            Optional<AuditSink> externalSink,
            AuditProperties properties,
            MeterRegistry meterRegistry) {

        log.info("Creating asynchronous processing pipeline with {} processors", processors.size());

        BlockingQueue<AuditEvent> queue =
                new ArrayBlockingQueue<>(properties.getProcessing().getAsync().getQueueCapacity());

        return new AsynchronousProcessingPipeline(
                processors,
                storage,
                externalSink,
                queue,
                meterRegistry
        );
    }

    /**
     * Virtual thread audit queue for async mode.
     */
    @Bean
    @ConditionalOnBean(AsynchronousProcessingPipeline.class)
    @ConditionalOnMissingBean
    public VirtualThreadAuditQueue virtualThreadAuditQueue(
            AuditProperties properties,
            AsynchronousProcessingPipeline pipeline,
            MeterRegistry meterRegistry) {

        var asyncConfig = properties.getProcessing().getAsync();
        var backpressureConfig = properties.getPerformance().getBackpressure();

        var backpressureHandler = new BackpressureHandler(
                backpressureConfig.getStrategy(),
                backpressureConfig.getThreshold()
        );

        log.info("Creating virtual thread audit queue: capacity={}, workers={}",
                asyncConfig.getQueueCapacity(),
                asyncConfig.getWorkerThreads());

        return new VirtualThreadAuditQueue(
                asyncConfig,
                pipeline,
                backpressureHandler,
                meterRegistry
        );
    }
}