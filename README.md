# SafeAudit

SafeAudit is a robust, compliance-focused audit logging framework for Spring Boot applications. It provides automatic
capturing of user actions, secure storage with integrity checks, and a built-in dashboard for monitoring and reporting.

## Features

- **🛡️ Secure & Compliant**: Implements PII masking, cryptographic chaining for log integrity, and configurable
  retention policies.
- **🚀 High Performance**: Asynchronous event processing with batching support to minimize impact on application latency.
- **🔌 Auto-Configuration**: Seamless integration with Spring Boot - just add the starter dependency.
- **📊 Built-in Dashboard**: Visualize audit events, view statistics, and export reports (PDF/CSV) without external
  tools.
- **💾 Flexible Storage**: Supports JDBC-compliant databases (H2, PostgreSQL, MySQL) with automatic schema management.
- **🔍 Granular Configuration**: Fine-tune what to capture (HTTP requests, specific methods) and how to process it.

## Quick Start

### Development Setup

```bash
# Clone the repository
git clone https://github.com/nelsontanko/safe-audit.git

# Build the project
mvn clean install

# Run tests
mvn test

# Run with Testcontainers (requires Docker)
mvn verify
```

### 1. Add Dependency

Add the SafeAudit starter to your project's `pom.xml`:

```xml

<dependency>
    <groupId>io.safeaudit</groupId>
    <artifactId>safeaudit-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Configuration (Optional)

SafeAudit is enabled by default with sensible defaults (H2 database storage, default dashboard path). You can customize
it in your `application.yml`:

```yaml
audit:
  enabled: true
  storage:
    type: DATABASE
    database:
      auto-create-schema: true

  reporting:
    ui:
      enabled: true
      path: /audit/dashboard
    api:
      base-path: /audit

  capture:
    http:
      enabled: true
      include-request-body: true
      exclude-patterns:
        - '/health'
        - '/metrics'

  processing:
    mode: ASYNC
    compliance:
      pii-masking:
        enabled: true
        strategy: HASH
```

## 📊 Accessing Audit Logs

### REST API

```bash
# Query audit events
GET /audit/events?userId=user123&from=2025-01-01T00:00:00Z

GET /audit/stats?userId=user123&from=2025-01-01T00:00:00Z

# Export to PDF
GET /audit/export?from=2025-01-01&to=2025-01-31&format=PDF

# Health check
GET /audit/health
```

### 3. Access Dashboard

Start your application and navigate to:
`http://localhost:8080/audit/dashboard`

## Architecture

SafeAudit is modular by design:

- **safeaudit-core**: Core domain models, event processing pipeline, and configuration properties.
- **safeaudit-persistence**: Storage implementations, SQL dialects, and schema management.
- **safeaudit-web**: REST APIs, Dashboard UI controller, and export functionality.
- **safeaudit-autoconfigure**: Spring Boot auto-configuration classes.
- **safeaudit-starter**: All-in-one dependency for easy integration.

## Usage

### Capturing Custom Events

While SafeAudit automatically captures HTTP requests, you can also record custom business events:

```java

@Autowired
private AuditProcessingPipeline auditPipeline;

public void processPayment(String orderId) {
    // ... business logic ...

    AuditEvent event = AuditEvent.builder()
            .action("PAYMENT_PROCESSED")
            .resource("ORDER", orderId)
            .severity(AuditSeverity.INFO)
            .detail("amount", amount)
            .build();

    auditPipeline.process(event);
}
```

## 🎯 Method-Level Auditing

```java

@Service
public class AccountService {

    @Audited(
            eventType = "ACCOUNT_TRANSFER",
            severity = AuditSeverity.CRITICAL,
            includeArgs = true
    )
    public void transferFunds(String fromAccount, String toAccount, BigDecimal amount) {
        // Your business logic
    }
}
```

## Monitoring & Integrity

SafeAudit runs periodic background tasks to verify the integrity of the audit log chain, ensuring that no records have
been tampered with or deleted. Violations are logged and can be viewed in the critical alerts section of the dashboard.

## 📞 Support

- 📧 Email: nelsonwisdomtanko@gmail.com
- 💬 Discussions: [GitHub Discussions](https://github.com/nelsontanko/safe-audit/discussions)
- 🐛 Issues: [GitHub Issues](https://github.com/nelsontanko/safe-audit/issues)

## 🗺️ Roadmap

### v1.0 (Current)

- [x] Core framework
- [x] PostgreSQL support
- [x] CBN & NDPA compliance
- [x] REST API & UI

### v1.1 (Planned)

- [ ] Kafka sink integration
- [ ] Elasticsearch integration
- [ ] GraphQL API
- [ ] Real-time WebSocket dashboard

### v2.0 (Future)

- [ ] ML-based anomaly detection
- [ ] Blockchain integration
- [ ] Multi-region replication
- [ ] Advanced analytics

---

