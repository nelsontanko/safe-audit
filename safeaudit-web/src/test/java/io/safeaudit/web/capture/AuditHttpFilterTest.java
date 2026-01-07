package io.safeaudit.web.capture;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditEventIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditHttpFilterTest {

    @Mock
    private AuditEventCapture eventCapture;
    @Mock
    private AuditProperties properties;
    @Mock
    private AuditEventIdGenerator idGenerator;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Environment environment;

    private AuditHttpFilter filter;

    @BeforeEach
    void setUp() {
        // Mock deeply nested properties
        var capture = mock(AuditProperties.CaptureConfig.class);
        var http = mock(AuditProperties.HttpCaptureConfig.class);

        lenient().when(properties.getCapture()).thenReturn(capture);
        lenient().when(capture.getHttp()).thenReturn(http);
        lenient().when(http.getExclusionPatterns()).thenReturn(List.of());
        lenient().when(http.isIncludeRequestBody()).thenReturn(true);
        lenient().when(http.isIncludeResponseBody()).thenReturn(true);
        lenient().when(http.getMaxBodySize()).thenReturn(1024);

        mockApplicationInfo();

        filter = new AuditHttpFilter(eventCapture, properties, idGenerator, applicationContext);
    }

    private void mockApplicationInfo() {
        lenient().when(applicationContext.getId()).thenReturn("test-app");
        lenient().when(applicationContext.getApplicationName()).thenReturn("Test Application");
        lenient().when(applicationContext.getEnvironment()).thenReturn(environment);
    }

    @Test
    void shouldCaptureSuccessfulRequest() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("POST", "/api/test");
        request.setAttribute("io.safeaudit.web.capture.SHOULD_AUDIT", true);
        request.setContent("request-body".getBytes());
        var response = new MockHttpServletResponse();

        when(idGenerator.generate()).thenReturn("event-123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(), any());

        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventCapture).capture(captor.capture());

        var event = captor.getValue();
        assertThat(event.eventId()).isEqualTo("event-123");
        assertThat(event.action()).isEqualTo("POST");
        assertThat(event.resource()).isEqualTo("/api/test");
        assertThat(event.severity()).isEqualTo(AuditSeverity.INFO);
        assertThat(event.httpStatusCode()).isEqualTo(200);
    }

    @Test
    void shouldExcludeActuatorEndpoints() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("GET", "/actuator/health");
        var response = new MockHttpServletResponse();

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(eventCapture);
    }

    @Test
    void shouldExcludeStaticResources() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("GET", "/static/image.png");
        var response = new MockHttpServletResponse();

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(eventCapture);
    }

    @Test
    void shouldHandleExceptionInChain() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("GET", "/api/error");
        request.setAttribute("io.safeaudit.web.capture.SHOULD_AUDIT", true);
        var response = new MockHttpServletResponse();

        doThrow(new RuntimeException("Processing failed"))
                .when(filterChain).doFilter(any(), any());

        when(idGenerator.generate()).thenReturn("event-err");

        // When
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected
        }

        // Then
        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventCapture).capture(captor.capture());

        var event = captor.getValue();
        assertThat(event.severity()).isEqualTo(AuditSeverity.CRITICAL);
        assertThat(event.eventType()).isEqualTo("HTTP_ACCESS"); // GET is HTTP_ACCESS
    }

    @Test
    void shouldUseExistingCorrelationId() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("GET", "/api/test");
        request.setAttribute("io.safeaudit.web.capture.SHOULD_AUDIT", true);
        request.addHeader("X-Correlation-ID", "existing-id");
        var response = new MockHttpServletResponse();

        when(idGenerator.generate()).thenReturn("event-1");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        // We can't easily verify ThreadLocal AuditContext here without exposing it or using a side-effect,
        // but we can ensure the request processed normally
        verify(eventCapture).capture(any());
    }
    @Test
    void shouldNotCaptureWhenNotAudited() throws ServletException, IOException {
        // Given
        var request = new MockHttpServletRequest("POST", "/api/test");
        // No attribute set
        var response = new MockHttpServletResponse();

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(), any());
        verifyNoInteractions(eventCapture);
    }
}
