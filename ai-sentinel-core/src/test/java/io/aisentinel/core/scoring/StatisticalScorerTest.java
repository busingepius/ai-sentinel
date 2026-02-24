package io.aisentinel.core.scoring;

import io.aisentinel.core.model.RequestFeatures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticalScorerTest {

    @Test
    void scoreReturnsZeroWhenInsufficientData() {
        var scorer = new StatisticalScorer();
        var f = RequestFeatures.builder()
            .identityHash("id")
            .endpoint("/api")
            .timestampMillis(0)
            .requestsPerWindow(1)
            .endpointEntropy(0)
            .tokenAgeSeconds(-1)
            .parameterCount(0)
            .payloadSizeBytes(0)
            .headerFingerprintHash(0)
            .ipBucket(0)
            .build();
        assertThat(scorer.score(f)).isEqualTo(0.0);
    }

    @Test
    void scoreIncreasesAfterAnomalousUpdate() {
        var scorer = new StatisticalScorer();
        var normal = RequestFeatures.builder()
            .identityHash("id")
            .endpoint("/api")
            .timestampMillis(0)
            .requestsPerWindow(1)
            .endpointEntropy(0)
            .tokenAgeSeconds(60)
            .parameterCount(2)
            .payloadSizeBytes(100)
            .headerFingerprintHash(10)
            .ipBucket(1)
            .build();
        for (int i = 0; i < 5; i++) {
            scorer.update(normal);
        }
        double scoreNormal = scorer.score(normal);

        var anomalous = RequestFeatures.builder()
            .identityHash("id")
            .endpoint("/api")
            .timestampMillis(0)
            .requestsPerWindow(1000)
            .endpointEntropy(5)
            .tokenAgeSeconds(60)
            .parameterCount(100)
            .payloadSizeBytes(10_000_000)
            .headerFingerprintHash(999)
            .ipBucket(999)
            .build();
        double scoreAnomalous = scorer.score(anomalous);

        assertThat(scoreAnomalous).isGreaterThan(scoreNormal);
        assertThat(scoreAnomalous).isBetween(0.0, 1.0);
    }
}
