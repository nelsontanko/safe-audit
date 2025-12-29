package io.safeaudit.persistence.dialect;

/**
 * @author Nelson Tanko
 */
public class PostgreSQLDialect implements SqlDialect {

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public String createTableDDL(String tableName) {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    event_id UUID NOT NULL,
                    sequence_number BIGINT NOT NULL,
                    event_timestamp TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                    event_type VARCHAR(50) NOT NULL,
                    severity VARCHAR(20) NOT NULL,
                
                    user_id VARCHAR(255),
                    username VARCHAR(255),
                    ip_address INET,
                    user_agent TEXT,
                
                    resource VARCHAR(500) NOT NULL,
                    action VARCHAR(100) NOT NULL,
                    session_id VARCHAR(255),
                    tenant_id VARCHAR(100),
                
                    request_payload TEXT,
                    response_payload TEXT,
                    http_status_code INTEGER,
                
                    compliance_tags TEXT[],
                    data_classification VARCHAR(50),
                    retention_until DATE,
                    contains_pii BOOLEAN DEFAULT FALSE,
                
                    previous_event_hash CHAR(64),
                    event_hash CHAR(64) NOT NULL,
                
                    captured_by VARCHAR(100) NOT NULL,
                    application_name VARCHAR(255) NOT NULL,
                    application_instance VARCHAR(255),
                
                    partition_key DATE NOT NULL GENERATED ALWAYS AS (DATE(event_timestamp)) STORED,
                
                    PRIMARY KEY (event_id, partition_key),
                    CONSTRAINT uk_sequence_per_instance UNIQUE (sequence_number, application_instance, partition_key)
                ) PARTITION BY RANGE (partition_key);
                
                CREATE INDEX IF NOT EXISTS idx_%s_timestamp ON %s (event_timestamp DESC);
                CREATE INDEX IF NOT EXISTS idx_%s_user ON %s (user_id, event_timestamp DESC);
                CREATE INDEX IF NOT EXISTS idx_%s_resource ON %s (resource, event_timestamp DESC);
                CREATE INDEX IF NOT EXISTS idx_%s_severity ON %s (severity, event_timestamp DESC) WHERE severity IN ('WARN', 'CRITICAL');
                CREATE INDEX IF NOT EXISTS idx_%s_compliance ON %s USING GIN (compliance_tags);
                """.formatted(
                tableName,
                tableName, tableName,
                tableName, tableName,
                tableName, tableName,
                tableName, tableName,
                tableName, tableName
        );
    }

    @Override
    public String createPartitionDDL(String tableName, String partitionName, String fromValue, String toValue) {
        return """
                CREATE TABLE IF NOT EXISTS %s PARTITION OF %s
                    FOR VALUES FROM ('%s') TO ('%s');
                """.formatted(partitionName, tableName, fromValue, toValue);
    }

    @Override
    public String insertSQL(String tableName) {
        return """
                INSERT INTO %s (
                    event_id, sequence_number, event_timestamp, event_type, severity,
                    user_id, username, ip_address, user_agent,
                    resource, action, session_id, tenant_id,
                    request_payload, response_payload, http_status_code,
                    compliance_tags, data_classification, retention_until, contains_pii,
                    previous_event_hash, event_hash,
                    captured_by, application_name, application_instance
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::inet, ?, ?, ?, ?, ?, ?, ?, ?, ?::text[], ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id, partition_key) DO NOTHING
                """.formatted(tableName);
    }

    @Override
    public String selectByIdSQL(String tableName) {
        return "SELECT * FROM %s WHERE event_id = ?".formatted(tableName);
    }

    @Override
    public String countSQL(String tableName, boolean hasWhere) {
        return "SELECT COUNT(*) FROM %s %s".formatted(tableName, hasWhere ? "WHERE" : "");
    }

    @Override
    public String selectSQL(String tableName, String whereClause, String orderBy, int limit, int offset) {
        var sql = new StringBuilder("SELECT * FROM ").append(tableName);

        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (orderBy != null && !orderBy.isBlank()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }

        if (offset > 0) {
            sql.append(" OFFSET ").append(offset);
        }

        return sql.toString();
    }

    @Override
    public boolean supportsPartitioning() {
        return true;
    }

    @Override
    public String getJsonType() {
        return "JSONB";
    }

    @Override
    public String getTimestampType() {
        return "TIMESTAMP(6) WITH TIME ZONE";
    }

    @Override
    public String getUuidType() {
        return "UUID";
    }
}
