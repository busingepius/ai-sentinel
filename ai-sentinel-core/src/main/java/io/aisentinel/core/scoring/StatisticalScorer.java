package io.aisentinel.core.scoring;

import io.aisentinel.core.model.RequestFeatures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statistical anomaly scorer using Welford's online algorithm for rolling mean/std.
 * Score is z-score based, normalized to [0.0, 1.0].
 */
public final class StatisticalScorer implements AnomalyScorer {

    private static final double MIN_STD = 1e-6;
    private static final double SIGMOID_SCALE = 3.0;

    private final Map<String, WelfordState> stateByKey = new ConcurrentHashMap<>();

    @Override
    public double score(RequestFeatures features) {
        double[] x = features.toArray();
        String key = features.identityHash() + "|" + features.endpoint();

        WelfordState state = stateByKey.get(key);
        if (state == null || state.n < 2) {
            return 0.0;
        }

        double[] stds = state.getStds();
        double maxZ = 0;
        for (int i = 0; i < x.length; i++) {
            double mean = state.means[i];
            double std = Math.max(stds[i], MIN_STD);
            double z = Math.abs((x[i] - mean) / std);
            maxZ = Math.max(maxZ, z);
        }

        return sigmoid(maxZ);
    }

    @Override
    public void update(RequestFeatures features) {
        double[] x = features.toArray();
        String key = features.identityHash() + "|" + features.endpoint();
        stateByKey.compute(key, (k, s) -> {
            WelfordState st = s != null ? s : new WelfordState(x.length);
            st.update(x);
            return st;
        });
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-SIGMOID_SCALE * (z - 2.0)));
    }

    private static final class WelfordState {
        final double[] means;
        final double[] m2;
        int n;

        WelfordState(int dim) {
            this.means = new double[dim];
            this.m2 = new double[dim];
        }

        void update(double[] x) {
            n++;
            for (int i = 0; i < x.length; i++) {
                double delta = x[i] - means[i];
                means[i] += delta / n;
                double delta2 = x[i] - means[i];
                m2[i] += delta * delta2;
            }
        }

        double[] getStds() {
            double[] stds = new double[means.length];
            for (int i = 0; i < means.length; i++) {
                stds[i] = n > 1 ? Math.sqrt(m2[i] / (n - 1)) : 0;
            }
            return stds;
        }
    }
}
