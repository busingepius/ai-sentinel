package io.aisentinel.core.policy;

import io.aisentinel.core.model.RequestFeatures;

/**
 * Maps risk score to enforcement action.
 */
public interface PolicyEngine {

    EnforcementAction evaluate(double riskScore, RequestFeatures features, String endpoint);
}
