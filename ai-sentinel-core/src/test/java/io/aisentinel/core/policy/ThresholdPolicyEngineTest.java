package io.aisentinel.core.policy;

import io.aisentinel.core.model.RequestFeatures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThresholdPolicyEngineTest {

    @Test
    void evaluateMapsScoreToAction() {
        var engine = new ThresholdPolicyEngine();
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

        assertThat(engine.evaluate(0.1, f, "/api")).isEqualTo(EnforcementAction.ALLOW);
        assertThat(engine.evaluate(0.3, f, "/api")).isEqualTo(EnforcementAction.MONITOR);
        assertThat(engine.evaluate(0.5, f, "/api")).isEqualTo(EnforcementAction.THROTTLE);
        assertThat(engine.evaluate(0.7, f, "/api")).isEqualTo(EnforcementAction.BLOCK);
        assertThat(engine.evaluate(0.9, f, "/api")).isEqualTo(EnforcementAction.QUARANTINE);
    }
}
