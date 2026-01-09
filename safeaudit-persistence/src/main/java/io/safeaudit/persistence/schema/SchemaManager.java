package io.safeaudit.persistence.schema;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.exception.AuditConfigurationException;
import io.safeaudit.persistence.PersistenceConstants;
import io.safeaudit.persistence.dialect.SqlDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class SchemaManager {

    private static final Logger log = LoggerFactory.getLogger(SchemaManager.class);

    private final JdbcTemplate jdbcTemplate;
    private final SqlDialect dialect;
    private final AuditProperties properties;
    private final String tableName;

    public SchemaManager(
            DataSource dataSource,
            SqlDialect dialect,
            AuditProperties properties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dialect = dialect;
        this.properties = properties;
        this.tableName = PersistenceConstants.DEFAULT_TABLE_NAME;
    }

    /**
     * Initialize schema if auto-create is enabled.
     */
    public void initialize() {
        if (!properties.getStorage().getDatabase().isAutoCreateSchema()) {
            log.info("Auto schema creation disabled");
            return;
        }

        if (tableExists(tableName)) {
            log.info("Audit table '{}' already exists", tableName);
            return;
        }

        createTable();
    }

    /**
     * Check if audit table exists.
     */
    public boolean tableExists(String tableName) {
        try {
            return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) conn -> {
                var metaData = conn.getMetaData();
                try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), null)) {
                    return rs.next();
                }
            }));
        } catch (Exception e) {
            log.warn("Failed to check table existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create audit table with indexes.
     */
    private void createTable() {
        try {
            log.info("Creating audit table '{}'", tableName);
            var ddl = dialect.createTableDDL(tableName);

            String[] statements = ddl.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                    log.debug("Executed: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
                }
            }

            log.info("Audit table '{}' created successfully", tableName);
        } catch (Exception e) {
            throw new AuditConfigurationException("Failed to create audit table", e);
        }
    }

    /**
     * Verify schema is valid.
     */
    public boolean verifySchema() {
        try {
            // Simple validation query
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + tableName + " WHERE 1=0",
                    Long.class
            );
            return true;
        } catch (Exception e) {
            log.error("Schema verification failed: {}", e.getMessage());
            return false;
        }
    }
}