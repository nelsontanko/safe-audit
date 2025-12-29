package io.safeaudit.persistence.jdbc;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.ComplianceMetadata;
import io.safeaudit.core.domain.HealthStatus;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.domain.enums.DataClassification;
import io.safeaudit.core.exception.AuditStorageException;
import io.safeaudit.core.exception.TransientStorageException;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.spi.IntegrityReport;
import io.safeaudit.core.spi.IntegrityReport.IntegrityViolation;
import io.safeaudit.core.spi.QueryCriteria;
import io.safeaudit.persistence.dialect.SqlDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Nelson Tanko
 */
public class JdbcAuditStorage implements AuditStorage {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuditStorage.class);
    private static final String DEFAULT_TABLE_NAME = "audit_events";

    private final JdbcTemplate jdbcTemplate;
    private final SqlDialect dialect;
    private final String tableName;
    private final AuditEventRowMapper rowMapper;

    public JdbcAuditStorage(DataSource dataSource, SqlDialect dialect) {
        this(dataSource, dialect, DEFAULT_TABLE_NAME);
    }

    public JdbcAuditStorage(DataSource dataSource, SqlDialect dialect, String tableName) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dialect = dialect;
        this.tableName = tableName;
        this.rowMapper = new AuditEventRowMapper();
    }

    @Override
    public boolean store(AuditEvent event) {
        try {
            var sql = dialect.insertSQL(tableName);
            int rows = jdbcTemplate.update(sql, ps -> setParameters(ps, event));
            return rows > 0;
        } catch (DuplicateKeyException _) {
            log.debug("Duplicate event ignored: {}", event.eventId());
            return false;
        } catch (DataAccessException e) {
            if (isTransient(e)) {
                throw new TransientStorageException("Transient error storing event", e);
            }
            throw new AuditStorageException("Failed to store event: " + event.eventId(), e);
        }
    }

    @Override
    public int storeBatch(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) {
            return 0;
        }

        try {
            var sql = dialect.insertSQL(tableName);

            int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    setParameters(ps, events.get(i));
                }

                @Override
                public int getBatchSize() {
                    return events.size();
                }
            });

            return Arrays.stream(results).sum();
        } catch (DataAccessException e) {
            if (isTransient(e)) {
                throw new TransientStorageException("Transient error storing batch", e);
            }
            throw new AuditStorageException("Failed to store batch of " + events.size(), e);
        }
    }

    @Override
    public Optional<AuditEvent> findById(String eventId) {
        try {
            var sql = dialect.selectByIdSQL(tableName);
            List<AuditEvent> results = jdbcTemplate.query(sql, rowMapper, UUID.fromString(eventId));
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        } catch (DataAccessException e) {
            throw new AuditStorageException("Failed to find event: " + eventId, e);
        }
    }

    @Override
    public List<AuditEvent> query(QueryCriteria criteria) {
        try {
            var builder = new QueryBuilder(criteria, dialect, tableName);
            var sql = builder.buildSelectSQL();
            Object[] params = builder.getParameters();

            return jdbcTemplate.query(sql, rowMapper, params);
        } catch (DataAccessException e) {
            throw new AuditStorageException("Failed to query events", e);
        }
    }

    @Override
    public long count(QueryCriteria criteria) {
        try {
            var builder = new QueryBuilder(criteria, dialect, tableName);
            var sql = builder.buildCountSQL();
            Object[] params = builder.getParameters();

            var count = jdbcTemplate.queryForObject(sql, Long.class, params);
            return count != null ? count : 0L;
        } catch (DataAccessException e) {
            throw new AuditStorageException("Failed to count events", e);
        }
    }

    @Override
    public IntegrityReport verifyIntegrity(Instant from, Instant to) {
        try {
            String sql = """
                    SELECT event_id, event_timestamp, event_hash, previous_event_hash
                    FROM %s
                    WHERE event_timestamp BETWEEN ? AND ?
                    ORDER BY event_timestamp ASC
                    """.formatted(tableName);

            List<IntegrityViolation> violations = new ArrayList<>();
            final String[] previousHash = {null};
            final AtomicLong[] totalEvents = {new AtomicLong()};
            final int[] verifiedEvents = {0};

            jdbcTemplate.query(sql, rs -> {
                var eventId = rs.getString("event_id");
                var timestamp = rs.getTimestamp("event_timestamp").toInstant();
                var eventHash = rs.getString("event_hash");
                var prevHash = rs.getString("previous_event_hash");

                totalEvents[0].getAndIncrement();

                if (previousHash[0] != null && !previousHash[0].equals(prevHash)) {
                    violations.add(new IntegrityViolation(
                            eventId,
                            "Hash chain broken: expected " + previousHash[0] + " but got " + prevHash,
                            timestamp
                    ));
                } else {
                    verifiedEvents[0]++;
                }

                previousHash[0] = eventHash;
            }, from, to);

            return new IntegrityReport(
                    violations.isEmpty(),
                    from,
                    to,
                    totalEvents[0].get(),
                    verifiedEvents[0],
                    violations
            );
        } catch (DataAccessException e) {
            throw new AuditStorageException("Failed to verify integrity", e);
        }
    }

    @Override
    public void initializeSchema() {
        try {
            log.info("Initializing audit schema for table: {}", tableName);
            var ddl = dialect.createTableDDL(tableName);

            // Execute each statement separately
            String[] statements = ddl.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                }
            }

            log.info("Audit schema initialized successfully");
        } catch (DataAccessException e) {
            throw new AuditStorageException("Failed to initialize schema", e);
        }
    }

    @Override
    public HealthStatus checkHealth() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return HealthStatus.healthy("JdbcAuditStorage");
        } catch (Exception e) {
            return HealthStatus.unhealthy("JdbcAuditStorage", e.getMessage());
        }
    }

    private void setParameters(PreparedStatement ps, AuditEvent event) throws SQLException {
        int idx = 1;

        ps.setObject(idx++, UUID.fromString(event.eventId()));
        ps.setLong(idx++, event.sequenceNumber());
        ps.setTimestamp(idx++, Timestamp.from(event.timestamp()));
        ps.setString(idx++, event.eventType());
        ps.setString(idx++, event.severity().name());

        ps.setString(idx++, event.userId());
        ps.setString(idx++, event.username());
        ps.setString(idx++, event.ipAddress());
        ps.setString(idx++, event.userAgent());

        ps.setString(idx++, event.resource());
        ps.setString(idx++, event.action());
        ps.setString(idx++, event.sessionId());
        ps.setString(idx++, event.tenantId());

        ps.setString(idx++, event.requestPayload());
        ps.setString(idx++, event.responsePayload());
        ps.setObject(idx++, event.httpStatusCode());

        // Compliance metadata
        ComplianceMetadata compliance = event.compliance();
        ps.setString(idx++, String.join(",", compliance.regulatoryTags()));
        ps.setString(idx++, compliance.dataClassification().name());
        ps.setDate(idx++, compliance.retentionUntil() != null ?
                Date.valueOf(compliance.retentionUntil()) : null);
        ps.setBoolean(idx++, compliance.containsPII());

        ps.setString(idx++, event.previousEventHash());
        ps.setString(idx++, event.eventHash());

        ps.setString(idx++, event.capturedBy());
        ps.setString(idx++, event.applicationName());
        ps.setString(idx++, event.applicationInstance());
    }

    private boolean isTransient(DataAccessException e) {
        // Check for transient error conditions
        Throwable cause = e.getCause();
        if (cause instanceof SQLException sqlEx) {
            String sqlState = sqlEx.getSQLState();
            // Connection errors, deadlocks, timeouts
            return sqlState != null &&
                    (sqlState.startsWith("08") ||  // Connection exception
                            sqlState.startsWith("40") ||  // Transaction rollback
                            sqlState.startsWith("57"));   // Operator intervention
        }
        return false;
    }

    /**
     * RowMapper for converting ResultSet to AuditEvent.
     */
    private static class AuditEventRowMapper implements RowMapper<AuditEvent> {

        @Override
        public AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            var compliance = ComplianceMetadata.builder()
                    .regulatoryTags(parseTags(rs.getString("compliance_tags")))
                    .dataClassification(DataClassification.valueOf(rs.getString("data_classification")))
                    .retentionUntil(rs.getDate("retention_until") != null ?
                            rs.getDate("retention_until").toLocalDate() : null)
                    .containsPII(rs.getBoolean("contains_pii"))
                    .build();

            return AuditEvent.builder()
                    .eventId(rs.getString("event_id"))
                    .sequenceNumber(rs.getLong("sequence_number"))
                    .timestamp(rs.getTimestamp("event_timestamp").toInstant())
                    .eventType(rs.getString("event_type"))
                    .severity(AuditSeverity.valueOf(rs.getString("severity")))
                    .userId(rs.getString("user_id"))
                    .username(rs.getString("username"))
                    .ipAddress(rs.getString("ip_address"))
                    .userAgent(rs.getString("user_agent"))
                    .resource(rs.getString("resource"))
                    .action(rs.getString("action"))
                    .sessionId(rs.getString("session_id"))
                    .tenantId(rs.getString("tenant_id"))
                    .requestPayload(rs.getString("request_payload"))
                    .responsePayload(rs.getString("response_payload"))
                    .httpStatusCode((Integer) rs.getObject("http_status_code"))
                    .compliance(compliance)
                    .previousEventHash(rs.getString("previous_event_hash"))
                    .eventHash(rs.getString("event_hash"))
                    .capturedBy(rs.getString("captured_by"))
                    .applicationName(rs.getString("application_name"))
                    .applicationInstance(rs.getString("application_instance"))
                    .build();
        }

        private Set<String> parseTags(String tags) {
            if (tags == null || tags.isBlank()) {
                return Set.of();
            }
            return Set.of(tags.split(","));
        }
    }

    /**
     * Helper class for building dynamic SQL queries.
     */
    private static class QueryBuilder {

        private final QueryCriteria criteria;
        private final SqlDialect dialect;
        private final String tableName;
        private final List<Object> parameters = new ArrayList<>();
        private final StringBuilder whereClause = new StringBuilder();

        public QueryBuilder(QueryCriteria criteria, SqlDialect dialect, String tableName) {
            this.criteria = criteria;
            this.dialect = dialect;
            this.tableName = tableName;
            buildWhereClause();
        }

        private void buildWhereClause() {
            boolean first = true;

            if (criteria.getEventId() != null) {
                appendCondition("event_id = ?", criteria.getEventId(), first);
                first = false;
            }

            if (criteria.getUserId() != null) {
                appendCondition("user_id = ?", criteria.getUserId(), first);
                first = false;
            }

            if (criteria.getUsername() != null) {
                appendCondition("username LIKE ?", "%" + criteria.getUsername() + "%", first);
                first = false;
            }

            if (criteria.getResource() != null) {
                appendCondition("resource LIKE ?", "%" + criteria.getResource() + "%", first);
                first = false;
            }

            if (criteria.getEventType() != null) {
                appendCondition("event_type = ?", criteria.getEventType(), first);
                first = false;
            }

            if (criteria.getTenantId() != null) {
                appendCondition("tenant_id = ?", criteria.getTenantId(), first);
                first = false;
            }

            if (criteria.getFrom() != null) {
                appendCondition("event_timestamp >= ?", Timestamp.from(criteria.getFrom()), first);
                first = false;
            }

            if (criteria.getTo() != null) {
                appendCondition("event_timestamp <= ?", Timestamp.from(criteria.getTo()), first);
                first = false;
            }

            if (!criteria.getSeverities().isEmpty()) {
                var placeholders = String.join(",",
                        Collections.nCopies(criteria.getSeverities().size(), "?"));
                appendCondition("severity IN (" + placeholders + ")", null, first);
                criteria.getSeverities().forEach(s -> parameters.add(s.name()));
            }
        }

        private void appendCondition(String condition, Object value, boolean first) {
            if (!first) {
                whereClause.append(" AND ");
            }
            whereClause.append(condition);
            if (value != null) {
                parameters.add(value);
            }
        }

        public String buildSelectSQL() {
            var orderBy = criteria.getSortBy() + " " + criteria.getSortDirection().name();
            return dialect.selectSQL(
                    tableName,
                    whereClause.toString(),
                    orderBy,
                    criteria.getSize(),
                    criteria.getPage() * criteria.getSize()
            );
        }

        public String buildCountSQL() {
            return dialect.countSQL(tableName, !whereClause.isEmpty()) +
                    (whereClause.isEmpty() ? "" : " " + whereClause);
        }

        public Object[] getParameters() {
            return parameters.toArray();
        }
    }
}