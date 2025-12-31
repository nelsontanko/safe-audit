package io.safeaudit.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 */
class UUIDv7GeneratorTest {
    private final UUIDv7Generator generator = new UUIDv7Generator();

    @Test
    void shouldGenerateValidUUID() {
        // When
        String uuid = generator.generate();

        // Then
        assertThat(uuid).isNotNull().matches("^[a-f0-9]{8}-[a-f0-9]{4}-7[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}$");
    }

    @Test
    void shouldGenerateUniqueUUIDs() {
        // Given
        Set<String> uuids = new HashSet<>();

        // When
        for (int i = 0; i < 1000; i++) {
            uuids.add(generator.generate());
        }

        // Then
        assertThat(uuids).hasSize(1000);
    }

    @Test
    void shouldGenerateTimeOrderedUUIDs() {
        // When
        String uuid1 = generator.generate();
        String uuid2 = generator.generate();
        String uuid3 = generator.generate();

        // Then - UUIDs should be lexicographically ordered
        assertThat(uuid1).isLessThan(uuid2);
        assertThat(uuid2).isLessThan(uuid3);
    }
}