package io.safeaudit.persistence.partition;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.persistence.dialect.SqlDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Manages table partitioning for audit events.
 * Automatically creates partitions based on configured strategy.
 *
 * @author Nelson Tanko
 */
public class PartitionManager {

    private static final Logger log = LoggerFactory.getLogger(PartitionManager.class);
    private static final String DEFAULT_TABLE_NAME = "audit_events";

    private final JdbcTemplate jdbcTemplate;
    private final SqlDialect dialect;
    private final AuditProperties.PartitioningConfig config;
    private final String tableName;

    public PartitionManager(
            DataSource dataSource,
            SqlDialect dialect,
            AuditProperties properties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dialect = dialect;
        this.config = properties.getStorage().getDatabase().getPartitioning();
        this.tableName = DEFAULT_TABLE_NAME;
    }

    /**
     * Initialize partitions for current and future periods.
     */
    public void initialize() {
        if (!config.isEnabled() || !dialect.supportsPartitioning()) {
            log.info("Partitioning disabled or not supported");
            return;
        }

        if (!config.isAutoCreate()) {
            log.info("Auto partition creation disabled");
            return;
        }

        createInitialPartitions();
    }

    /**
     * Create partitions for current and next 3 periods.
     */
    private void createInitialPartitions() {
        var current = LocalDate.now();

        for (int i = 0; i < 4; i++) {
            var partitionDate = advanceDate(current, i);
            createPartition(partitionDate);
        }
    }

    /**
     * Scheduled job to create future partitions.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void createFuturePartitions() {
        if (!config.isEnabled() || !config.isAutoCreate()) {
            return;
        }

        var futureDate = advanceDate(LocalDate.now(), 2);
        createPartition(futureDate);
    }

    /**
     * Create partition for a specific date.
     */
    public void createPartition(LocalDate date) {
        if (!dialect.supportsPartitioning()) {
            return;
        }

        var partitionName = generatePartitionName(date);

        if (partitionExists(partitionName)) {
            log.debug("Partition {} already exists", partitionName);
            return;
        }

        try {
            var fromDate = getPartitionStart(date);
            var toDate = getPartitionEnd(date);

            var ddl = dialect.createPartitionDDL(
                    tableName,
                    partitionName,
                    fromDate.toString(),
                    toDate.toString()
            );

            if (ddl != null && !ddl.isBlank()) {
                jdbcTemplate.execute(ddl);
                log.info("Created partition: {} for period [{}, {})",
                        partitionName, fromDate, toDate);
            }
        } catch (Exception e) {
            log.error("Failed to create partition {}: {}", partitionName, e.getMessage());
        }
    }

    /**
     * Check if partition exists.
     */
    private boolean partitionExists(String partitionName) {
        try {
            String sql = switch (dialect.getDatabaseType()) {
                case "PostgreSQL" -> """
                        SELECT COUNT(*) FROM pg_tables
                        WHERE tablename = ?
                        """;
                case "MySQL" -> """
                        SELECT COUNT(*) FROM information_schema.PARTITIONS
                        WHERE TABLE_NAME = ? AND PARTITION_NAME = ?
                        """;
                default -> null;
            };

            if (sql == null) {
                return false;
            }

            var count = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    dialect.getDatabaseType().equals("MySQL") ?
                            new Object[]{tableName, partitionName} :
                            new Object[]{partitionName}
            );

            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Failed to check partition existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get partition start date based on strategy.
     */
    private LocalDate getPartitionStart(LocalDate date) {
        return switch (config.getStrategy()) {
            case DAILY -> date;
            case MONTHLY -> date.withDayOfMonth(1);
            case YEARLY -> date.withDayOfYear(1);
        };
    }

    /**
     * Get partition end date based on strategy.
     */
    private LocalDate getPartitionEnd(LocalDate date) {
        return switch (config.getStrategy()) {
            case DAILY -> date.plusDays(1);
            case MONTHLY -> date.withDayOfMonth(1).plusMonths(1);
            case YEARLY -> date.withDayOfYear(1).plusYears(1);
        };
    }

    /**
     * Advance date by N periods based on strategy.
     */
    private LocalDate advanceDate(LocalDate date, int periods) {
        return switch (config.getStrategy()) {
            case DAILY -> date.plusDays(periods);
            case MONTHLY -> date.plusMonths(periods);
            case YEARLY -> date.plusYears(periods);
        };
    }

    /**
     * Generate partition name based on date and strategy.
     */
    private String generatePartitionName(LocalDate date) {
        String format = switch (config.getStrategy()) {
            case DAILY -> "yyyyMMdd";
            case MONTHLY -> "yyyyMM";
            case YEARLY -> "yyyy";
        };

        return tableName + "_" + date.format(DateTimeFormatter.ofPattern(format));
    }
}
