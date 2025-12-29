package io.safeaudit.persistence.dialect;

/**
 * @author Nelson Tanko
 */
public class H2Dialect implements SqlDialect {

    @Override
    public String getDatabaseType() {
        return "H2";
    }

    @Override
    public String createTableDDL(String tableName) {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    event_id UUID NOT NULL PRIMARY KEY,
                    sequence_number BIGINT NOT NULL,
                    event_timestamp TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                    event_type VARCHAR(50) NOT NULL,
                    severity VARCHAR(20) NOT NULL,
                
                    user_id VARCHAR(255),
                    username VARCHAR(255),
                    ip_address VARCHAR(45),
                    user_agent CLOB,
                
                    resource VARCHAR(500) NOT NULL,
                    action VARCHAR(100) NOT NULL,
                    session_id VARCHAR(255),
                    tenant_id VARCHAR(100),
                
                    request_payload CLOB,
                    response_payload CLOB,
                    http_status_code INT,
                
                    compliance_tags VARCHAR(1000),
                    data_classification VARCHAR(50),
                    retention_until DATE,
                    contains_pii BOOLEAN DEFAULT FALSE,
                
                    previous_event_hash CHAR(64),
                    event_hash CHAR(64) NOT NULL,
                
                    captured_by VARCHAR(100) NOT NULL,
                    application_name VARCHAR(255) NOT NULL,
                    application_instance VARCHAR(255)
                );
                
                CREATE INDEX IF NOT EXISTS idx_%s_timestamp ON %s (event_timestamp DESC);
                CREATE INDEX IF NOT EXISTS idx_%s_user ON %s (user_id, event_timestamp DESC);
                """.formatted(tableName, tableName, tableName, tableName, tableName);
    }

    @Override
    public String createPartitionDDL(String tableName, String partitionName, String fromValue, String toValue) {
        return ""; // H2 doesn't support partitioning
    }

    @Override
    public String insertSQL(String tableName) {
        return """
                MERGE INTO %s (
                    event_id, sequence_number, event_timestamp, event_type, severity,
                    user_id, username, ip_address, user_agent,
                    resource, action, session_id, tenant_id,
                    request_payload, response_payload, http_status_code,
                    compliance_tags, data_classification, retention_until, contains_pii,
                    previous_event_hash, event_hash,
                    captured_by, application_name, application_instance
                ) KEY(event_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            if (offset > 0) {
                sql.append(" OFFSET ").append(offset);
            }
        }

        return sql.toString();
    }

    @Override
    public boolean supportsPartitioning() {
        return false;
    }

    @Override
    public String getJsonType() {
        return "VARCHAR(10000)";
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