package io.aisentinel.core.policy;

import io.aisentinel.core.model.RequestFeatures;

/**
 * Policy engine mapping score thresholds to actions.
 * Default thresholds: Low &lt;0.2, Moderate &lt;0.4, Elevated &lt;0.6, High &lt;0.8, Critical &gt;=0.8
 */
public final class ThresholdPolicyEngine implements PolicyEngine {

    private final double thresholdModerate;
    private final double thresholdElevated;
    private final double thresholdHigh;
    private final double thresholdCritical;

    public ThresholdPolicyEngine() {
        this(0.2, 0.4, 0.6, 0.8);
    }

    public ThresholdPolicyEngine(double moderate, double elevated, double high, double critical) {
        this.thresholdModerate = moderate;
        this.thresholdElevated = elevated;
        this.thresholdHigh = high;
        this.thresholdCritical = critical;
    }

    @Override
    public EnforcementAction evaluate(double riskScore, RequestFeatures features, String endpoint) {
        if (riskScore >= thresholdCritical) return EnforcementAction.QUARANTINE;
        if (riskScore >= thresholdHigh) return EnforcementAction.BLOCK;
        if (riskScore >= thresholdElevated) return EnforcementAction.THROTTLE;
        if (riskScore >= thresholdModerate) return EnforcementAction.MONITOR;
        return EnforcementAction.ALLOW;
    }
}
