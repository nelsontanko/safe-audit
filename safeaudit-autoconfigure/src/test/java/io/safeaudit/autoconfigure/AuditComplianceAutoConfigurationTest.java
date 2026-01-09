package io.safeaudit.autoconfigure;

import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.processing.compliance.CBNComplianceProfile;
import io.safeaudit.core.processing.compliance.NDPAComplianceProfile;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.schema.SchemaManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditComplianceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditComplianceAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void shouldActivateCBNProfile() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.processing.compliance.regulations=CBN")
                .run(context -> {
                    assertThat(context).hasSingleBean(CBNComplianceProfile.class);
                    assertThat(context).doesNotHaveBean(NDPAComplianceProfile.class);
                });
    }

    @Test
    void shouldActivateNDPAProfile() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.processing.compliance.regulations=NDPA")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CBNComplianceProfile.class);
                    assertThat(context).hasSingleBean(NDPAComplianceProfile.class);
                });
    }

    @Test
    void shouldNotActivateProfilesByDefault() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CBNComplianceProfile.class);
                    assertThat(context).doesNotHaveBean(NDPAComplianceProfile.class);
                });
    }

    @Configuration
    static class StorageConfiguration {
        @Bean
        public AuditStorage auditStorage() {
            return mock(AuditStorage.class);
        }

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class); // Required by AuditAutoConfiguration imports sometimes
        }

        @Bean
        public SchemaManager schemaManager() {
            return mock(SchemaManager.class);
        }

        @Bean
        public AuditEventCapture auditEventCapture() {
            return mock(AuditEventCapture.class);
        }

        @Bean
        public AuditProcessingPipeline auditProcessingPipeline() {
            return mock(AuditProcessingPipeline.class);
        }
    }
}
