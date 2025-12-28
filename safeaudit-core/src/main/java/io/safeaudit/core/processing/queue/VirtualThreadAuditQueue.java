package io.safeaudit.core.processing.queue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.processing.AsynchronousProcessingPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async queue using virtual threads (Java 21+) for processing audit events.
 * Falls back to platform threads if virtual threads not available.
 *
 * @author Nelson Tanko
 */
public class VirtualThreadAuditQueue {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadAuditQueue.class);

    private final BlockingQueue<AuditEvent> queue;
    private final ExecutorService executor;
    private final AsynchronousProcessingPipeline pipeline;
    private final BackpressureHandler backpressureHandler;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final Counter enqueuedCounter;
    private final Counter droppedCounter;

    public VirtualThreadAuditQueue(
            AuditProperties.AsyncConfig config,
            AsynchronousProcessingPipeline pipeline,
            BackpressureHandler backpressureHandler,
            MeterRegistry meterRegistry) {

        this.queue = new ArrayBlockingQueue<>(config.getQueueCapacity());
        this.pipeline = pipeline;
        this.backpressureHandler = backpressureHandler;

        this.executor = createExecutor(config.getWorkerThreads());

        this.enqueuedCounter = Counter.builder("audit.queue.enqueued")
                .description("Events enqueued for processing")
                .register(meterRegistry);

        this.droppedCounter = Counter.builder("audit.queue.dropped")
                .description("Events dropped due to backpressure")
                .register(meterRegistry);

        Gauge.builder("audit.queue.size", queue, BlockingQueue::size)
                .description("Current queue size")
                .register(meterRegistry);

        startWorkers(config.getWorkerThreads());

        log.info("Audit queue started with {} workers, capacity {}",
                config.getWorkerThreads(), config.getQueueCapacity());
    }

    private ExecutorService createExecutor(int workers) {
        try {
            var factory = Thread.class.getMethod("ofVirtual").invoke(null);
            var builder = factory.getClass().getMethod("factory").invoke(factory);
            return (ExecutorService) Executors.class
                    .getMethod("newThreadPerTaskExecutor", ThreadFactory.class)
                    .invoke(null, builder);
        } catch (Exception _) {
            log.info("Virtual threads not available, using platform threads");
            return Executors.newFixedThreadPool(workers);
        }
    }

    private void startWorkers(int count) {
        for (int i = 0; i < count; i++) {
            executor.submit(this::processLoop);
        }
    }

    private void processLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                AuditEvent event = queue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    pipeline.process(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing audit event", e);
            }
        }
    }

    public void enqueue(AuditEvent event) {
        if (!running.get()) {
            throw new IllegalStateException("Queue is shut down");
        }

        boolean added = queue.offer(event);

        if (added) {
            enqueuedCounter.increment();
        } else {
            droppedCounter.increment();
            backpressureHandler.handle(event, queue.size());
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void shutdown() {
        running.set(false);
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Audit queue shut down");
    }
}
