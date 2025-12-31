package io.safeaudit.core.domain;

import io.safeaudit.core.domain.enums.DataClassification;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Nelson Tanko
 */
class ComplianceMetadataTest {

    @Test
    void shouldCreateEmptyComplianceMetadata() {
        // When
        var metadata = ComplianceMetadata.empty();

        // Then
        assertThat(metadata.regulatoryTags()).isEmpty();
        assertThat(metadata.dataClassification()).isEqualTo(DataClassification.INTERNAL);
        assertThat(metadata.containsPII()).isFalse();
    }

    @Test
    void shouldBuildWithRegulatoryTags() {
        // When
        var metadata = ComplianceMetadata.builder()
                .addRegulatoryTag("CBN")
                .addRegulatoryTag("NDPA")
                .build();

        // Then
        assertThat(metadata.regulatoryTags()).containsExactlyInAnyOrder("CBN", "NDPA");
    }

    @Test
    void shouldBuildWithDataClassification() {
        // When
        var metadata = ComplianceMetadata.builder()
                .dataClassification(DataClassification.RESTRICTED)
                .build();

        // Then
        assertThat(metadata.dataClassification()).isEqualTo(DataClassification.RESTRICTED);
    }

    @Test
    void shouldBuildWithRetentionDate() {
        // Given
        var retentionDate = LocalDate.now().plusYears(7);

        // When
        var metadata = ComplianceMetadata.builder()
                .retentionUntil(retentionDate)
                .build();

        // Then
        assertThat(metadata.retentionUntil()).isEqualTo(retentionDate);
    }

    @Test
    void shouldBuildWithPIIFlag() {
        // When
        var metadata = ComplianceMetadata.builder()
                .containsPII(true)
                .build();

        // Then
        assertThat(metadata.containsPII()).isTrue();
    }

    @Test
    void shouldBuildWithProcessingPurposes() {
        // When
        var metadata = ComplianceMetadata.builder()
                .addProcessingPurpose("REGULATORY_COMPLIANCE")
                .addProcessingPurpose("AUDIT")
                .build();

        // Then
        assertThat(metadata.processingPurposes())
                .containsExactlyInAnyOrder("REGULATORY_COMPLIANCE", "AUDIT");
    }

    @Test
    void shouldReturnImmutableRegulatoryTags() {
        // Given
        var metadata = ComplianceMetadata.builder()
                .addRegulatoryTag("CBN")
                .build();

        // When/Then
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> metadata.regulatoryTags().add("NDPA"));
    }
}

