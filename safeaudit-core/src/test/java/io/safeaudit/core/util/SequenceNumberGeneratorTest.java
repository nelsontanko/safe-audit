package io.safeaudit.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SequenceNumberGeneratorTest {

    @Test
    void shouldGenerateSequentialNumbers() {
        // Given
        var generator = new SequenceNumberGenerator();

        // When
        long first = generator.next();
        long second = generator.next();
        long third = generator.next();

        // Then
        assertThat(first).isEqualTo(1L);
        assertThat(second).isEqualTo(2L);
        assertThat(third).isEqualTo(3L);
    }

    @Test
    void shouldReturnCurrentWithoutIncrementing() {
        // Given
        var generator = new SequenceNumberGenerator();
        generator.next();

        // When
        long current1 = generator.current();
        long current2 = generator.current();

        // Then
        assertThat(current1).isEqualTo(1L);
        assertThat(current2).isEqualTo(1L);
    }

    @Test
    void shouldResetSequence() {
        // Given
        var generator = new SequenceNumberGenerator();
        generator.next();
        generator.next();

        // When
        generator.reset(100L);

        // Then
        assertThat(generator.current()).isEqualTo(100L);
        assertThat(generator.next()).isEqualTo(101L);
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        // Given
        var generator = new SequenceNumberGenerator();
        int threadCount = 10;
        int iterationsPerThread = 1000;
        var latch = new CountDownLatch(threadCount);
        Set<Long> numbers = ConcurrentHashMap.newKeySet();

        // When
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    numbers.add(generator.next());
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        // Then
        assertThat(numbers).hasSize(threadCount * iterationsPerThread);
        assertThat(generator.current()).isEqualTo(threadCount * iterationsPerThread);
    }
}