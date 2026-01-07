package io.safeaudit.core.processing;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.processing.enrichment.CorrelationIdEnricher;
import io.safeaudit.core.processing.enrichment.UserContextEnricher;
import io.safeaudit.core.processing.integrity.HashCalculator;
import io.safeaudit.core.spi.AuditEventProcessor;
import io.safeaudit.core.spi.AuditStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditProcessingPipelineIntegrationTest {

    @Test
    void shouldProcessEventThroughCompletePipeline() {
        // Given
        var storage = mock(AuditStorage.class);
        when(storage.store(any())).thenReturn(true);

        List<AuditEventProcessor> processors = List.of(
                new UserContextEnricher(),
                new CorrelationIdEnricher(),
                new HashCalculator("SHA-256", true)
        );

        var pipeline = new SynchronousProcessingPipeline(
                processors,
                storage,
                Optional.empty(),
                new SimpleMeterRegistry()
        );

        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        // When
        pipeline.process(event);

        // Then
        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(storage).store(captor.capture());

        var stored = captor.getValue();
        assertThat(stored.sessionId()).isNotNull(); // From CorrelationIdEnricher
        assertThat(stored.eventHash()).isNotNull(); // From HashCalculator
    }
}