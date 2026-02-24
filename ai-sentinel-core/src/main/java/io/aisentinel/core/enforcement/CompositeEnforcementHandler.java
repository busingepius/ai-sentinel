package io.aisentinel.core.enforcement;

import io.aisentinel.core.policy.EnforcementAction;
import io.aisentinel.core.telemetry.TelemetryEmitter;
import io.aisentinel.core.telemetry.TelemetryEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Composite enforcement handler with Allow, Monitor, Throttle, Block, Quarantine.
 */
@Slf4j
public final class CompositeEnforcementHandler implements EnforcementHandler {

    private final int blockStatusCode;
    private final long quarantineDurationMs;
    private final double throttleRequestsPerSecond;
    private final Map<String, AtomicLong> throttleTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> quarantinedUntil = new ConcurrentHashMap<>();
    private final TelemetryEmitter telemetry;

    public CompositeEnforcementHandler(int blockStatusCode, long quarantineDurationMs,
                                       double throttleRequestsPerSecond, TelemetryEmitter telemetry) {
        this.blockStatusCode = blockStatusCode;
        this.quarantineDurationMs = quarantineDurationMs;
        this.throttleRequestsPerSecond = Math.max(0.1, throttleRequestsPerSecond);
        this.telemetry = telemetry;
    }

    @Override
    public boolean apply(EnforcementAction action, HttpServletRequest request, HttpServletResponse response,
                         String identityHash, String endpoint) {
        return switch (action) {
            case ALLOW -> true;
            case MONITOR -> {
                telemetry.emit(TelemetryEvent.policyActionApplied(identityHash, endpoint, "MONITOR", null));
                yield true;
            }
            case THROTTLE -> applyThrottle(request, response, identityHash, endpoint);
            case BLOCK -> {
                applyBlock(response, identityHash, endpoint);
                yield false;
            }
            case QUARANTINE -> {
                applyQuarantine(response, identityHash, endpoint);
                yield false;
            }
        };
    }

    @Override
    public boolean isQuarantined(String identityHash) {
        Long until = quarantinedUntil.get(identityHash);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            quarantinedUntil.remove(identityHash);
            return false;
        }
        return true;
    }

    public boolean checkThrottle(String identityHash, String endpoint) {
        String key = identityHash + "|" + endpoint;
        long now = System.nanoTime();
        long refillNs = (long) (1_000_000_000.0 / throttleRequestsPerSecond);
        AtomicLong nextAllowed = throttleTokens.computeIfAbsent(key, k -> new AtomicLong(0));
        for (; ; ) {
            long prev = nextAllowed.get();
            if (now < prev) return false;
            if (nextAllowed.compareAndSet(prev, now + refillNs)) return true;
        }
    }

    private boolean applyThrottle(HttpServletRequest request, HttpServletResponse response,
                                  String identityHash, String endpoint) {
        if (!checkThrottle(identityHash, endpoint)) {
            try {
                response.setStatus(429);
                response.getWriter().write("Too Many Requests");
            } catch (Exception ignored) { }
            telemetry.emit(TelemetryEvent.policyActionApplied(identityHash, endpoint, "THROTTLE_APPLIED", "429"));
            return false;
        }
        telemetry.emit(TelemetryEvent.policyActionApplied(identityHash, endpoint, "THROTTLE_ALLOW", null));
        return true;
    }

    private void applyBlock(HttpServletResponse response, String identityHash, String endpoint) {
        log.debug("Blocking request for endpoint={} identityHash={}", endpoint, maskHash(identityHash));
        try {
            response.setStatus(blockStatusCode);
            response.getWriter().write(blockStatusCode == 403 ? "Forbidden" : "Too Many Requests");
        } catch (Exception ignored) { }
        telemetry.emit(TelemetryEvent.policyActionApplied(identityHash, endpoint, "BLOCK", String.valueOf(blockStatusCode)));
    }

    private void applyQuarantine(HttpServletResponse response, String identityHash, String endpoint) {
        log.debug("Quarantining identityHash={} for endpoint={} durationMs={}", maskHash(identityHash), endpoint, quarantineDurationMs);
        quarantinedUntil.put(identityHash, System.currentTimeMillis() + quarantineDurationMs);
        try {
            response.setStatus(blockStatusCode);
            response.getWriter().write("Quarantined");
        } catch (Exception ignored) { }
        telemetry.emit(TelemetryEvent.quarantineStarted(identityHash, endpoint, quarantineDurationMs));
    }

    private static String maskHash(String h) {
        if (h == null || h.length() < 8) return "***";
        return h.substring(0, 4) + "***" + h.substring(h.length() - 4);
    }
}
