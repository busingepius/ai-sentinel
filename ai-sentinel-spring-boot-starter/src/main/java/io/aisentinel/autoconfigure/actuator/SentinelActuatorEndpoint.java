package io.aisentinel.autoconfigure.actuator;

import io.aisentinel.autoconfigure.config.SentinelProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Map;

@Slf4j
@Endpoint(id = "sentinel")
@RequiredArgsConstructor
public class SentinelActuatorEndpoint {

    private final SentinelProperties props;

    @ReadOperation
    public Map<String, Object> info() {
        log.trace("Actuator /actuator/sentinel info requested");
        return Map.of(
            "enabled", props.isEnabled(),
            "mode", props.getMode().name(),
            "isolationForestEnabled", props.getIsolationForest().isEnabled()
        );
    }
}
