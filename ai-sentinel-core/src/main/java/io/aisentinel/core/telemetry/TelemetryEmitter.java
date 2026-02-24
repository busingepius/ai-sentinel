package io.aisentinel.core.telemetry;

/**
 * Emits telemetry events (fire-and-forget; failures must not affect request processing).
 */
public interface TelemetryEmitter {

    void emit(TelemetryEvent event);
}
