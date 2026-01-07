package io.safeaudit.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class IPAddressExtractorTest {

    @Test
    void shouldExtractFromXForwardedFor() {
        // Given
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

        // When
        var ip = IPAddressExtractor.extract(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldExtractFirstIPFromMultipleInXForwardedFor() {
        // Given
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");

        // When
        var ip = IPAddressExtractor.extract(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldFallbackToRemoteAddr() {
        // Given
        var request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        // When
        var ip = IPAddressExtractor.extract(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.100");
    }

    @Test
    void shouldReturnNullForNullRequest() {
        // When
        var ip = IPAddressExtractor.extract(null);

        // Then
        assertThat(ip).isNull();
    }

    @Test
    void shouldSkipUnknownValues() {
        // Given
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");

        // When
        var ip = IPAddressExtractor.extract(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.1");
    }
}