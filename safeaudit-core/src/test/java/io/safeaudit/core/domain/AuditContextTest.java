package io.safeaudit.core.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditContextTest {

    @AfterEach
    void cleanup() {
        AuditContext.clear();
    }

    @Test
    void shouldStoreAndRetrieveValue() {
        // Given
        var key = "test-key";
        var value = "test-value";

        // When
        AuditContext.set(key, value);
        var retrieved = AuditContext.get(key, String.class).orElse(null);

        // Then
        assertThat(retrieved).isEqualTo(value);
    }

    @Test
    void shouldReturnEmptyForMissingKey() {
        // When
        var result = AuditContext.get("non-existent", String.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForWrongType() {
        // Given
        AuditContext.set("key", "string-value");

        // When
        var result = AuditContext.get("key", Integer.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldClearContext() {
        // Given
        AuditContext.set("key1", "value1");
        AuditContext.set("key2", "value2");

        // When
        AuditContext.clear();

        // Then
        assertThat(AuditContext.get("key1")).isEmpty();
        assertThat(AuditContext.get("key2")).isEmpty();
    }

    @Test
    void shouldGetAllContextValues() {
        // Given
        AuditContext.set("key1", "value1");
        AuditContext.set("key2", 123);

        // When
        var allValues = AuditContext.getAll();

        // Then
        assertThat(allValues).containsKeys("key1", "key2");
        assertThat(allValues.get("key1")).isEqualTo("value1");
        assertThat(allValues.get("key2")).isEqualTo(123);
    }

    @Test
    void shouldIsolateThreadLocalValues() throws InterruptedException {
        // Given
        AuditContext.set("key", "main-thread-value");

        // When
        var otherThread = new Thread(() -> {
            AuditContext.set("key", "other-thread-value");
            assertThat(AuditContext.get("key", String.class)).contains("other-thread-value");
        });

        otherThread.start();
        otherThread.join();

        // Then
        assertThat(AuditContext.get("key", String.class)).contains("main-thread-value");
    }
}
