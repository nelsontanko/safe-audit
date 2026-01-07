package io.safeaudit.persistence;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.dialect.SqlDialect;
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
        var dialect = detectDialect(dataSource);
        return new JdbcAuditStorage(dataSource, dialect);
    }

    /**
     * Resolve SQL dialect using properties or auto-detection.
     */
    public static SqlDialect resolveDialect(DataSource dataSource, AuditProperties properties) {
        String manualDialect = properties.getStorage().getDatabase().getDialect();
        if (manualDialect != null && !"AUTO".equalsIgnoreCase(manualDialect)) {
            log.info("Using manually configured SQL dialect: {}", manualDialect);
            return SqlDialectFactory.create(DatabaseType.fromProductName(manualDialect));
        }
        return detectDialect(dataSource);
    }

    /**
     * Detect SQL dialect from DataSource.
     */
    public static SqlDialect detectDialect(DataSource dataSource) {
        var databaseType = detectDatabaseType(dataSource);
        log.info("Detected SQL dialect: {}", databaseType);
        return SqlDialectFactory.create(databaseType);
    }

    /**
     * Detect database type from DataSource.
     */
    private static DatabaseType detectDatabaseType(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                log.warn("DataSource returned null connection, falling back to H2");
                return DatabaseType.H2;
            }
            var metaData = conn.getMetaData();
            if (metaData == null) {
                log.warn("Connection returned null metadata, falling back to H2");
                return DatabaseType.H2;
            }
            var productName = metaData.getDatabaseProductName();
//            var productVersion = metaData.getDatabaseProductVersion();

            log.info("Database detected: {}", productName);

            return DatabaseType.fromProductName(productName);
        } catch (Exception e) {
            log.warn("Failed to detect database type via metadata: {}. Falling back to H2", e.getMessage());
            return DatabaseType.H2;
        }
    }
}