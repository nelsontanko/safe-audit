package io.safeaudit.web.capture;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditEventIdGenerator;
import io.safeaudit.core.util.SequenceNumberGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditMethodInterceptorTest {

    @Mock
    private AuditEventCapture eventCapture;
    @Mock
    private AuditEventIdGenerator idGenerator;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature signature;
    @Mock
    private Audited audited;
    @Mock
    private SequenceNumberGenerator sequenceNumberGenerator;
    @Mock
    private Environment environment;

    private AuditMethodInterceptor interceptor;

    @BeforeEach
    void setUp() {
        mockApplicationInfo();
        interceptor = new AuditMethodInterceptor(eventCapture, idGenerator, applicationContext, sequenceNumberGenerator);
    }

    private void mockApplicationInfo() {
        lenient().when(applicationContext.getId()).thenReturn("test-app");
        lenient().when(applicationContext.getApplicationName()).thenReturn("Test Application");
        lenient().when(applicationContext.getEnvironment()).thenReturn(environment);
        lenient().when(environment.getProperty("spring.application.name")).thenReturn("Test Application");
    }

    @Test
    void shouldCaptureMethodExecution() throws Throwable {
        // Given
        var args = new Object[]{"arg1", 123};
        var result = "result-value";

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(result);

        when(signature.getName()).thenReturn("testMethod");

        when(audited.eventType()).thenReturn("TEST_EXECUTION");
        when(audited.resource()).thenReturn("test-resource");
        when(audited.severity()).thenReturn(AuditSeverity.INFO);
        when(audited.includeArgs()).thenReturn(true);
        when(audited.includeResult()).thenReturn(true);

        when(idGenerator.generate()).thenReturn("event-1");

        // When
        var actualResult = interceptor.auditMethod(joinPoint, audited);

        // Then
        assertThat(actualResult).isEqualTo(result);

        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventCapture).capture(captor.capture());

        var event = captor.getValue();
        assertThat(event.eventType()).isEqualTo("TEST_EXECUTION");
        assertThat(event.resource()).isEqualTo("test-resource");
        assertThat(event.action()).isEqualTo("testMethod");
        assertThat(event.severity()).isEqualTo(AuditSeverity.INFO);
        assertThat(event.requestPayload()).contains("arg1", "123");
        assertThat(event.responsePayload()).contains("result-value");
    }

    @Test
    void shouldCaptureException() throws Throwable {
        // Given
        var method = TestClass.class.getMethod("testMethod", String.class, int.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Failure"));

        when(signature.getMethod()).thenReturn(method);
        when(signature.getName()).thenReturn("testMethod");
        when(signature.getDeclaringTypeName()).thenReturn(TestClass.class.getName());

        when(audited.eventType()).thenReturn("");
        when(audited.resource()).thenReturn("");
        when(audited.includeArgs()).thenReturn(false);
        when(audited.includeResult()).thenReturn(false);

        when(idGenerator.generate()).thenReturn("event-err");

        // When/Then
        assertThrows(RuntimeException.class, () -> interceptor.auditMethod(joinPoint, audited));

        var captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventCapture).capture(captor.capture());

        var event = captor.getValue();
        assertThat(event.eventType()).isEqualTo("TESTMETHOD"); // Uppercase method name default
        assertThat(event.severity()).isEqualTo(AuditSeverity.CRITICAL); // Exception implies CRITICAL
        assertThat(event.action()).isEqualTo("testMethod");
    }

    // Helper class for reflection
    static class TestClass {
        public void testMethod(String s, int i) {
            // this method is empty
        }
    }
}
