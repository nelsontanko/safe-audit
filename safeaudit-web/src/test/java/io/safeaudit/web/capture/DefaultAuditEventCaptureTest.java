package io.safeaudit.web.capture;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultAuditEventCaptureTest {

    @Mock
    private AuditProcessingPipeline pipeline;

    @InjectMocks
    private DefaultAuditEventCapture eventCapture;

    @Test
    void shouldDelegateToPipeline() {
        // Given
        var event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TEST")
                .timestamp(Instant.now())
                .build();

        // When
        eventCapture.capture(event);

        // Then
        verify(pipeline).process(event);
    }
}
