package io.safeaudit.persistence;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.exception.AuditConfigurationException;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.dialect.SqlDialectFactory;
import io.safeaudit.persistence.jdbc.JdbcAuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author Nelson Tanko
 */
public final class AuditStorageFactory {

    private static final Logger log = LoggerFactory.getLogger(AuditStorageFactory.class);

    private AuditStorageFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Create AuditStorage instance from DataSource.
     */
    public static AuditStorage create(DataSource dataSource, AuditProperties properties) {
        var databaseType = detectDatabaseType(dataSource);
        log.info("Detected database type: {}", databaseType);

        var dialect = SqlDialectFactory.create(databaseType);

        return new JdbcAuditStorage(dataSource, dialect);
    }

    /**
     * Detect database type from DataSource.
     */
    private static DatabaseType detectDatabaseType(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            var metaData = conn.getMetaData();
            var productName = metaData.getDatabaseProductName();

            log.debug("Database product name: {}", productName);

            return DatabaseType.fromProductName(productName);
        } catch (Exception e) {
            throw new AuditConfigurationException("Failed to detect database type", e);
        }
    }
}