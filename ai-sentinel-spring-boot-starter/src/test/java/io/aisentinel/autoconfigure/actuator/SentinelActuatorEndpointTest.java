package io.aisentinel.autoconfigure.actuator;

import io.aisentinel.autoconfigure.config.SentinelProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelActuatorEndpointTest {

    @Test
    void infoReturnsExpectedStructure() {
        SentinelProperties props = new SentinelProperties();
        SentinelActuatorEndpoint endpoint = new SentinelActuatorEndpoint(props);

        Map<String, Object> info = endpoint.info();

        assertThat(info).containsKeys("enabled", "mode", "isolationForestEnabled");
        assertThat(info.get("enabled")).isEqualTo(true);
        assertThat(info.get("mode")).isEqualTo("ENFORCE");
        assertThat(info.get("isolationForestEnabled")).isEqualTo(false);
    }

    @Test
    void infoReflectsCustomProperties() {
        SentinelProperties props = new SentinelProperties();
        props.setEnabled(false);
        props.setMode(SentinelProperties.Mode.MONITOR);
        props.getIsolationForest().setEnabled(true);
        SentinelActuatorEndpoint endpoint = new SentinelActuatorEndpoint(props);

        Map<String, Object> info = endpoint.info();

        assertThat(info.get("enabled")).isEqualTo(false);
        assertThat(info.get("mode")).isEqualTo("MONITOR");
        assertThat(info.get("isolationForestEnabled")).isEqualTo(true);
    }
}
