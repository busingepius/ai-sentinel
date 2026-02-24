package io.aisentinel.core.scoring;

import io.aisentinel.core.model.RequestFeatures;

/**
 * Computes an anomaly score for request features.
 * Score is normalized to [0.0, 1.0] where higher = more anomalous.
 */
public interface AnomalyScorer {

    double score(RequestFeatures features);

    void update(RequestFeatures features);
}
