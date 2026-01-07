package io.safeaudit.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.safeaudit.core.domain.enums.DataClassification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public record ComplianceMetadata(
        Set<String> regulatoryTags, DataClassification dataClassification,

        LocalDate retentionUntil, boolean containsPII, String consentReference,

        Set<String> processingPurposes
) {

    @JsonCreator
    public ComplianceMetadata(
            @JsonProperty("regulatoryTags") Set<String> regulatoryTags,
            @JsonProperty("dataClassification") DataClassification dataClassification,
            @JsonProperty("retentionUntil") LocalDate retentionUntil,
            @JsonProperty("containsPII") boolean containsPII,
            @JsonProperty("consentReference") String consentReference,
            @JsonProperty("processingPurposes") Set<String> processingPurposes) {

        this.regulatoryTags = regulatoryTags != null ?
                Set.copyOf(regulatoryTags) :
                Collections.emptySet();
        this.dataClassification = dataClassification != null ?
                dataClassification :
                DataClassification.INTERNAL;
        this.retentionUntil = retentionUntil;
        this.containsPII = containsPII;
        this.consentReference = consentReference;
        this.processingPurposes = processingPurposes != null ?
                Set.copyOf(processingPurposes) :
                Collections.emptySet();
    }

    public static ComplianceMetadata empty() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<String> regulatoryTags = new HashSet<>();
        private DataClassification dataClassification = DataClassification.INTERNAL;
        private LocalDate retentionUntil;
        private boolean containsPII;
        private String consentReference;
        private final Set<String> processingPurposes = new HashSet<>();

        public Builder addRegulatoryTag(String tag) {
            this.regulatoryTags.add(tag);
            return this;
        }

        public Builder regulatoryTags(Set<String> tags) {
            this.regulatoryTags.clear();
            if (tags != null) {
                this.regulatoryTags.addAll(tags);
            }
            return this;
        }

        public Builder dataClassification(DataClassification classification) {
            this.dataClassification = classification;
            return this;
        }

        public Builder retentionUntil(LocalDate date) {
            this.retentionUntil = date;
            return this;
        }

        public Builder containsPII(boolean containsPII) {
            this.containsPII = containsPII;
            return this;
        }

        public Builder consentReference(String reference) {
            this.consentReference = reference;
            return this;
        }

        public Builder addProcessingPurpose(String purpose) {
            this.processingPurposes.add(purpose);
            return this;
        }

        public Builder processingPurposes(Set<String> purposes) {
            this.processingPurposes.clear();
            if (purposes != null) {
                this.processingPurposes.addAll(purposes);
            }
            return this;
        }

        public ComplianceMetadata build() {
            return new ComplianceMetadata(
                    regulatoryTags,
                    dataClassification,
                    retentionUntil,
                    containsPII,
                    consentReference,
                    processingPurposes
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplianceMetadata that = (ComplianceMetadata) o;
        return containsPII == that.containsPII &&
                Objects.equals(regulatoryTags, that.regulatoryTags) &&
                dataClassification == that.dataClassification &&
                Objects.equals(retentionUntil, that.retentionUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regulatoryTags, dataClassification, retentionUntil, containsPII);
    }
}


