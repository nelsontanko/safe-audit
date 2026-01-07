package io.safeaudit.autoconfigure;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.AuditStorageFactory;
import io.safeaudit.persistence.dialect.SqlDialect;
import io.safeaudit.persistence.partition.PartitionManager;
import io.safeaudit.persistence.retention.RetentionPolicy;
import io.safeaudit.persistence.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableScheduling
public class AuditStorageAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditStorageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(SqlDialect.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(
            prefix = "audit.storage",
            name = "type",
            havingValue = "DATABASE",
            matchIfMissing = true)
    public SqlDialect sqlDialect(DataSource dataSource, AuditProperties properties) {
        return AuditStorageFactory.resolveDialect(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean(AuditStorage.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(
            prefix = "audit.storage",
            name = "type",
            havingValue = "DATABASE",
            matchIfMissing = true)
    public AuditStorage jdbcAuditStorage(DataSource dataSource, SqlDialect dialect, AuditProperties properties) {
        log.info("Initializing JDBC audit storage with dialect: {}", dialect.getDatabaseType());
        return new io.safeaudit.persistence.jdbc.JdbcAuditStorage(dataSource, dialect);
    }

    /**
     * Fallback audit storage when database is not available.
     */
    @Bean
    @ConditionalOnMissingBean(AuditStorage.class)
    public AuditStorage loggingAuditStorage() {
        log.info("Creating fallback Logging audit storage");
        return new io.safeaudit.core.spi.LoggingAuditStorage();
    }

    /**
     * Schema manager for automatic table creation.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({DataSource.class, SqlDialect.class})
    public SchemaManager schemaManager(
            DataSource dataSource,
            SqlDialect dialect,
            AuditProperties properties) {
        return new SchemaManager(dataSource, dialect, properties);
    }

    /**
     * Partition manager for automatic partition creation.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({DataSource.class, SqlDialect.class})
    @ConditionalOnProperty(
            prefix = "audit.storage.database.partitioning",
            name = "enabled",
            havingValue = "true")
    public PartitionManager partitionManager(
            DataSource dataSource,
            SqlDialect dialect,
            AuditProperties properties) {

        return new PartitionManager(dataSource, dialect, properties);
    }

    /**
     * Retention policy for data lifecycle management.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(
            prefix = "audit.storage.database.retention",
            name = "enabled",
            havingValue = "true")
    public RetentionPolicy retentionPolicy(DataSource dataSource, AuditProperties properties) {
        return new RetentionPolicy(dataSource, properties);
    }

    /**
     * Initialize schema on startup.
     */
    @Bean
    public ApplicationRunner auditSchemaInitializer(AuditStorage storage, AuditProperties properties) {
        return args -> {
            if (properties.getStorage().getDatabase().isAutoCreateSchema()) {
                log.info("Checking audit storage schema...");
                storage.initializeSchema();
            }
        };
    }

    /**
     * Initialize partitions on startup.
     */
    @Bean
    @ConditionalOnBean(PartitionManager.class)
    public ApplicationRunner partitionInitializer(PartitionManager partitionManager) {
        return args -> {
            log.info("Initializing audit partitions...");
            partitionManager.initialize();
        };
    }

}