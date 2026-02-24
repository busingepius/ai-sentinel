package io.aisentinel.core.scoring;

import io.aisentinel.core.model.RequestFeatures;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted combination of anomaly scorers.
 * When IsolationForest is disabled, only StatisticalScorer is used.
 */
public final class CompositeScorer implements AnomalyScorer {

    private final List<WeightedScorer> scorers = new ArrayList<>();

    public void addScorer(AnomalyScorer scorer, double weight) {
        if (weight > 0) {
            scorers.add(new WeightedScorer(scorer, weight));
        }
    }

    @Override
    public double score(RequestFeatures features) {
        if (scorers.isEmpty()) return 0.0;
        double sum = 0;
        double totalWeight = 0;
        for (WeightedScorer ws : scorers) {
            double s = ws.scorer.score(features);
            sum += s * ws.weight;
            totalWeight += ws.weight;
        }
        return totalWeight > 0 ? Math.min(1.0, sum / totalWeight) : 0.0;
    }

    @Override
    public void update(RequestFeatures features) {
        for (WeightedScorer ws : scorers) {
            ws.scorer.update(features);
        }
    }

    private record WeightedScorer(AnomalyScorer scorer, double weight) {}
}
