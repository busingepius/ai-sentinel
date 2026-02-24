package io.aisentinel.autoconfigure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "ai.sentinel")
public class SentinelProperties {

    public enum Mode { OFF, MONITOR, ENFORCE }

    private boolean enabled = true;
    private Mode mode = Mode.ENFORCE;
    private List<String> excludePaths = List.of("/actuator/**", "/health", "/health/**", "/static/**", "/favicon.ico");
    private int blockStatusCode = 429;
    private long quarantineDurationMs = 300_000;
    private double throttleRequestsPerSecond = 5.0;
    private Duration baselineTtl = Duration.ofMinutes(5);
    private int baselineMaxKeys = 100_000;

    private IsolationForest isolationForest = new IsolationForest();
    private Telemetry telemetry = new Telemetry();

    @Data
    public static class Telemetry {
        private String logVerbosity = "ANOMALY_ONLY";
        private double logScoreThreshold = 0.4;
        private int logSampleRate = 100;

        public void setLogSampleRate(int v) {
            this.logSampleRate = Math.max(1, v);
        }
    }

    @Data
    public static class IsolationForest {
        private boolean enabled = false;
    }
}
