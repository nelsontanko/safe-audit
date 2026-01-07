package io.safeaudit.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        // Given
        var properties = new AuditProperties();

        // Then
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getCapture().getHttp().isEnabled()).isTrue();
        assertThat(properties.getProcessing().getMode())
                .isEqualTo(AuditProperties.ProcessingMode.ASYNC);
        assertThat(properties.getStorage().getType())
                .isEqualTo(AuditProperties.StorageType.DATABASE);
    }

    @Test
    void shouldConfigureHttpCapture() {
        // Given
        var properties = new AuditProperties();
        var httpConfig = properties.getCapture().getHttp();

        // When
        httpConfig.setEnabled(false);
        httpConfig.setMaxBodySize(20480);

        // Then
        assertThat(httpConfig.isEnabled()).isFalse();
        assertThat(httpConfig.getMaxBodySize()).isEqualTo(20480);
    }

    @Test
    void shouldConfigureAsyncProcessing() {
        // Given
        var properties = new AuditProperties();
        var asyncConfig = properties.getProcessing().getAsync();

        // When
        asyncConfig.setQueueCapacity(5000);
        asyncConfig.setWorkerThreads(8);

        // Then
        assertThat(asyncConfig.getQueueCapacity()).isEqualTo(5000);
        assertThat(asyncConfig.getWorkerThreads()).isEqualTo(8);
    }

    @Test
    void shouldConfigurePIIMasking() {
        // Given
        var properties = new AuditProperties();
        var piiConfig = properties.getProcessing().getCompliance().getPiiMasking();

        // When
        piiConfig.setStrategy(AuditProperties.PIIMaskingStrategy.REDACT);
        piiConfig.getFields().add("customField");

        // Then
        assertThat(piiConfig.getStrategy())
                .isEqualTo(AuditProperties.PIIMaskingStrategy.REDACT);
        assertThat(piiConfig.getFields()).contains("customField");
    }
}