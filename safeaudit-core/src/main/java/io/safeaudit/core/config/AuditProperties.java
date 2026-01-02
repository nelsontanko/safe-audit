package io.safeaudit.core.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nelson Tanko
 */
@ConfigurationProperties(prefix = "audit")
public class AuditProperties {

    private boolean enabled = true;
    private CaptureConfig capture = new CaptureConfig();
    private ProcessingConfig processing = new ProcessingConfig();
    private StorageConfig storage = new StorageConfig();
    private ReportingConfig reporting = new ReportingConfig();
    private IntegrityConfig integrity = new IntegrityConfig();
    private PerformanceConfig performance = new PerformanceConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CaptureConfig getCapture() {
        return capture;
    }

    public void setCapture(CaptureConfig capture) {
        this.capture = capture;
    }

    public ProcessingConfig getProcessing() {
        return processing;
    }

    public void setProcessing(ProcessingConfig processing) {
        this.processing = processing;
    }

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }

    public ReportingConfig getReporting() {
        return reporting;
    }

    public void setReporting(ReportingConfig reporting) {
        this.reporting = reporting;
    }

    public IntegrityConfig getIntegrity() {
        return integrity;
    }

    public void setIntegrity(IntegrityConfig integrity) {
        this.integrity = integrity;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    public static class CaptureConfig {
        private HttpCaptureConfig http = new HttpCaptureConfig();
        private MethodCaptureConfig method = new MethodCaptureConfig();
        private EventCaptureConfig event = new EventCaptureConfig();

        public HttpCaptureConfig getHttp() {
            return http;
        }

        public void setHttp(HttpCaptureConfig http) {
            this.http = http;
        }

        public MethodCaptureConfig getMethod() {
            return method;
        }

        public void setMethod(MethodCaptureConfig method) {
            this.method = method;
        }

        public EventCaptureConfig getEvent() {
            return event;
        }

        public void setEvent(EventCaptureConfig event) {
            this.event = event;
        }
    }

    public static class HttpCaptureConfig {
        private boolean enabled = true;
        private boolean includeRequestBody = true;
        private boolean includeResponseBody = false;

        @Min(1024)
        private int maxBodySize = 10240; // 10KB

        private List<String> exclusionPatterns = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeRequestBody() {
            return includeRequestBody;
        }

        public void setIncludeRequestBody(boolean includeRequestBody) {
            this.includeRequestBody = includeRequestBody;
        }

        public boolean isIncludeResponseBody() {
            return includeResponseBody;
        }

        public void setIncludeResponseBody(boolean includeResponseBody) {
            this.includeResponseBody = includeResponseBody;
        }

        public int getMaxBodySize() {
            return maxBodySize;
        }

        public void setMaxBodySize(int maxBodySize) {
            this.maxBodySize = maxBodySize;
        }

        public List<String> getExclusionPatterns() {
            return exclusionPatterns;
        }

        public void setExclusionPatterns(List<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
        }
    }

    public static class MethodCaptureConfig {
        private boolean enabled = true;
        private List<String> basePackages = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getBasePackages() {
            return basePackages;
        }

        public void setBasePackages(List<String> basePackages) {
            this.basePackages = basePackages;
        }
    }

    public static class EventCaptureConfig {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ProcessingConfig {
        @NotNull
        private ProcessingMode mode = ProcessingMode.ASYNC;

        private AsyncConfig async = new AsyncConfig();
        private EnrichmentConfig enrichment = new EnrichmentConfig();
        private ComplianceConfig compliance = new ComplianceConfig();

        public ProcessingMode getMode() {
            return mode;
        }

        public void setMode(ProcessingMode mode) {
            this.mode = mode;
        }

        public AsyncConfig getAsync() {
            return async;
        }

        public void setAsync(AsyncConfig async) {
            this.async = async;
        }

        public EnrichmentConfig getEnrichment() {
            return enrichment;
        }

        public void setEnrichment(EnrichmentConfig enrichment) {
            this.enrichment = enrichment;
        }

        public ComplianceConfig getCompliance() {
            return compliance;
        }

        public void setCompliance(ComplianceConfig compliance) {
            this.compliance = compliance;
        }
    }

    public enum ProcessingMode {
        SYNC, ASYNC
    }

    public static class AsyncConfig {
        @Min(100)
        private int queueCapacity = 10000;

        @Min(1)
        private int workerThreads = 4;

        @Min(1)
        private int batchSize = 100;

        @Min(100)
        private long batchTimeoutMs = 5000;

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getWorkerThreads() {
            return workerThreads;
        }

        public void setWorkerThreads(int workerThreads) {
            this.workerThreads = workerThreads;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getBatchTimeoutMs() {
            return batchTimeoutMs;
        }

        public void setBatchTimeoutMs(long batchTimeoutMs) {
            this.batchTimeoutMs = batchTimeoutMs;
        }

        public Duration getBatchTimeout() {
            return Duration.ofMillis(batchTimeoutMs);
        }
    }

    public static class EnrichmentConfig {
        private boolean enabled = true;
        private boolean userContext = true;
        private boolean correlationId = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isUserContext() {
            return userContext;
        }

        public void setUserContext(boolean userContext) {
            this.userContext = userContext;
        }

        public boolean isCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(boolean correlationId) {
            this.correlationId = correlationId;
        }
    }

    public static class ComplianceConfig {
        private PIIMaskingConfig piiMasking = new PIIMaskingConfig();
        private Set<String> regulations = new HashSet<>();
        private DataClassificationConfig dataClassification = new DataClassificationConfig();

        public PIIMaskingConfig getPiiMasking() {
            return piiMasking;
        }

        public void setPiiMasking(PIIMaskingConfig piiMasking) {
            this.piiMasking = piiMasking;
        }

        public Set<String> getRegulations() {
            return regulations;
        }

        public void setRegulations(Set<String> regulations) {
            this.regulations = regulations;
        }

        public DataClassificationConfig getDataClassification() {
            return dataClassification;
        }

        public void setDataClassification(DataClassificationConfig dataClassification) {
            this.dataClassification = dataClassification;
        }
    }

    public static class PIIMaskingConfig {
        private boolean enabled = true;
        private Set<String> fields = new HashSet<>();
        private PIIMaskingStrategy strategy = PIIMaskingStrategy.HASH;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Set<String> getFields() {
            return fields;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        public PIIMaskingStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(PIIMaskingStrategy strategy) {
            this.strategy = strategy;
        }
    }

    public enum PIIMaskingStrategy {
        HASH, MASK, REDACT
    }

    public static class DataClassificationConfig {
        private boolean autoClassify = true;
        private String defaultLevel = "INTERNAL";

        public boolean isAutoClassify() {
            return autoClassify;
        }

        public void setAutoClassify(boolean autoClassify) {
            this.autoClassify = autoClassify;
        }

        public String getDefaultLevel() {
            return defaultLevel;
        }

        public void setDefaultLevel(String defaultLevel) {
            this.defaultLevel = defaultLevel;
        }
    }

    public static class StorageConfig {
        private StorageType type = StorageType.DATABASE;
        private DatabaseConfig database = new DatabaseConfig();

        public StorageType getType() {
            return type;
        }

        public void setType(StorageType type) {
            this.type = type;
        }

        public DatabaseConfig getDatabase() {
            return database;
        }

        public void setDatabase(DatabaseConfig database) {
            this.database = database;
        }
    }

    public enum StorageType {
        DATABASE, FILE
    }

    public static class DatabaseConfig {
        private boolean autoCreateSchema = true;
        private String dialect = "AUTO";
        private boolean useApplicationDataSource = true;
        private ConnectionConfig connection = new ConnectionConfig();
        private PartitioningConfig partitioning = new PartitioningConfig();
        private RetentionConfig retention = new RetentionConfig();

        public boolean isAutoCreateSchema() {
            return autoCreateSchema;
        }

        public void setAutoCreateSchema(boolean autoCreateSchema) {
            this.autoCreateSchema = autoCreateSchema;
        }

        public String getDialect() {
            return dialect;
        }

        public void setDialect(String dialect) {
            this.dialect = dialect;
        }

        public boolean isUseApplicationDataSource() {
            return useApplicationDataSource;
        }

        public void setUseApplicationDataSource(boolean useApplicationDataSource) {
            this.useApplicationDataSource = useApplicationDataSource;
        }

        public ConnectionConfig getConnection() {
            return connection;
        }

        public void setConnection(ConnectionConfig connection) {
            this.connection = connection;
        }

        public PartitioningConfig getPartitioning() {
            return partitioning;
        }

        public void setPartitioning(PartitioningConfig partitioning) {
            this.partitioning = partitioning;
        }

        public RetentionConfig getRetention() {
            return retention;
        }

        public void setRetention(RetentionConfig retention) {
            this.retention = retention;
        }
    }

    public static class ConnectionConfig {
        @Min(1)
        private int poolSize = 5;

        @Min(1000)
        private long timeoutMs = 5000;

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    public static class PartitioningConfig {
        private boolean enabled = true;
        private PartitionStrategy strategy = PartitionStrategy.MONTHLY;
        private boolean autoCreate = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public PartitionStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(PartitionStrategy strategy) {
            this.strategy = strategy;
        }

        public boolean isAutoCreate() {
            return autoCreate;
        }

        public void setAutoCreate(boolean autoCreate) {
            this.autoCreate = autoCreate;
        }
    }

    public enum PartitionStrategy {
        DAILY, MONTHLY, YEARLY
    }

    public static class RetentionConfig {
        private boolean enabled = true;

        @Min(1)
        private int defaultDays = 2555; // 7 years for CBN compliance

        private boolean archivalEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDefaultDays() {
            return defaultDays;
        }

        public void setDefaultDays(int defaultDays) {
            this.defaultDays = defaultDays;
        }

        public boolean isArchivalEnabled() {
            return archivalEnabled;
        }

        public void setArchivalEnabled(boolean archivalEnabled) {
            this.archivalEnabled = archivalEnabled;
        }
    }

    public static class ReportingConfig {
        private ApiConfig api = new ApiConfig();
        private UIConfig ui = new UIConfig();
        private ExportConfig export = new ExportConfig();

        public ApiConfig getApi() {
            return api;
        }

        public void setApi(ApiConfig api) {
            this.api = api;
        }

        public UIConfig getUi() {
            return ui;
        }

        public void setUi(UIConfig ui) {
            this.ui = ui;
        }

        public ExportConfig getExport() {
            return export;
        }

        public void setExport(ExportConfig export) {
            this.export = export;
        }
    }

    public static class ApiConfig {
        private boolean enabled = true;
        private String basePath = "/audit";
        private SecurityConfig security = new SecurityConfig();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public SecurityConfig getSecurity() {
            return security;
        }

        public void setSecurity(SecurityConfig security) {
            this.security = security;
        }
    }

    public static class SecurityConfig {
        private boolean enabled = true;
        private List<String> requiredRoles = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }

    public static class UIConfig {
        private boolean enabled = true;
        private String path = "/audit/dashboard";
        private String theme = "DARK";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }
    }

    public static class ExportConfig {
        private boolean enabled = true;
        private List<String> formats = List.of("PDF", "CSV");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getFormats() {
            return formats;
        }

        public void setFormats(List<String> formats) {
            this.formats = formats;
        }
    }

    public static class IntegrityConfig {
        private HashingConfig hashing = new HashingConfig();
        private VerificationConfig verification = new VerificationConfig();

        public HashingConfig getHashing() {
            return hashing;
        }

        public void setHashing(HashingConfig hashing) {
            this.hashing = hashing;
        }

        public VerificationConfig getVerification() {
            return verification;
        }

        public void setVerification(VerificationConfig verification) {
            this.verification = verification;
        }
    }

    public static class HashingConfig {
        private boolean enabled = true;
        private String algorithm = "SHA-256";
        private boolean includePreviousHash = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public boolean isIncludePreviousHash() {
            return includePreviousHash;
        }

        public void setIncludePreviousHash(boolean includePreviousHash) {
            this.includePreviousHash = includePreviousHash;
        }
    }

    public static class VerificationConfig {
        private boolean enabled = true;
        private String schedule = "0 0 2 * * ?"; // Daily at 2 AM

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSchedule() {
            return schedule;
        }

        public void setSchedule(String schedule) {
            this.schedule = schedule;
        }
    }

    public static class PerformanceConfig {
        private BackpressureConfig backpressure = new BackpressureConfig();
        private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

        public BackpressureConfig getBackpressure() {
            return backpressure;
        }

        public void setBackpressure(BackpressureConfig backpressure) {
            this.backpressure = backpressure;
        }

        public CircuitBreakerConfig getCircuitBreaker() {
            return circuitBreaker;
        }

        public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }
    }

    public static class BackpressureConfig {
        private boolean enabled = true;
        private int threshold = 8000;
        private BackpressureStrategy strategy = BackpressureStrategy.DROP_OLDEST;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public BackpressureStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(BackpressureStrategy strategy) {
            this.strategy = strategy;
        }
    }

    public enum BackpressureStrategy {
        DROP_OLDEST, BLOCK, REJECT
    }

    public static class CircuitBreakerConfig {
        private boolean enabled = true;
        private int failureThreshold = 5;
        private long timeoutMs = 60000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}