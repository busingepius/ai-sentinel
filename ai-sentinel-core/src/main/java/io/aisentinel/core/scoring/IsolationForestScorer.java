package io.aisentinel.core.scoring;

import io.aisentinel.core.model.RequestFeatures;

/**
 * Isolation Forest-based anomaly scorer (stub in v1).
 * Disabled by default; enable via ai.sentinel.isolation-forest.enabled=true.
 */
public final class IsolationForestScorer implements AnomalyScorer {

    @Override
    public double score(RequestFeatures features) {
        return 0.0;
    }

    @Override
    public void update(RequestFeatures features) {
        // no-op in stub
    }
}
