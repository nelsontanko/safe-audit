package io.safeaudit.persistence.retention;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.persistence.PersistenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.time.LocalDate;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class RetentionPolicy {

    private static final Logger log = LoggerFactory.getLogger(RetentionPolicy.class);

    private final JdbcTemplate jdbcTemplate;
    private final AuditProperties.RetentionConfig config;
    private final String tableName;

    public RetentionPolicy(DataSource dataSource, AuditProperties properties) {
        this(new JdbcTemplate(dataSource), properties);
    }

    RetentionPolicy(JdbcTemplate jdbcTemplate, AuditProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.config = properties.getStorage().getDatabase().getRetention();
        this.tableName = PersistenceConstants.DEFAULT_TABLE_NAME;
    }

    /**
     * Scheduled job to archive old data.
     * Runs weekly on Sunday at 3 AM.
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    @SuppressWarnings("java:S2077")
    public void archiveOldData() {
        if (!config.isEnabled() || !config.isArchivalEnabled()) {
            return;
        }

        var archivalDate = LocalDate.now().minusDays(config.getDefaultDays());

        try {
            log.info("Archiving audit data older than {}", archivalDate);

            // Move old data to archive table
            var sql = """
                    INSERT INTO %s_archive
                    SELECT * FROM %s
                    WHERE event_timestamp < ?
                    AND event_id NOT IN (SELECT event_id FROM %s_archive)
                    """.formatted(tableName, tableName, tableName);

            int archived = jdbcTemplate.update(sql, archivalDate);

            if (archived > 0) {
                log.info("Archived {} audit events", archived);

                // Delete archived data from main table
                var deleteSql = """
                        DELETE FROM %s
                        WHERE event_timestamp < ?
                        """.formatted(tableName);

                int deleted = jdbcTemplate.update(deleteSql, archivalDate);
                log.info("Deleted {} archived events from main table", deleted);
            }
        } catch (Exception e) {
            log.error("Failed to archive old data: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete data beyond retention period (use with caution).
     */
    @SuppressWarnings("java:S2077")
    public int purgeExpiredData() {
        if (!config.isEnabled()) {
            return 0;
        }

        var expirationDate = LocalDate.now().minusDays(config.getDefaultDays());

        try {
            log.warn("PURGING audit data older than {}", expirationDate);

            var sql = """
                    DELETE FROM %s
                    WHERE retention_until IS NOT NULL
                    AND retention_until < ?
                    """.formatted(tableName);

            int deleted = jdbcTemplate.update(sql, expirationDate);
            log.info("Purged {} expired audit events", deleted);

            return deleted;
        } catch (Exception e) {
            log.error("Failed to purge expired data: {}", e.getMessage(), e);
            return 0;
        }
    }
}

