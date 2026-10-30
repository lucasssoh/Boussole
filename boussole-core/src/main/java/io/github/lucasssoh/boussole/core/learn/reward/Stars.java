package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;

/**
 * How well an answer did, out of {@link #MAX}: the share of its {@link QuestionScore} it earned.
 * All of it gives 3 stars, at least half 2, anything 1, nothing 0. A question that earns nothing
 * (a free exercise graded by the learner, a hook) takes the share of its verdict: 1 when right,
 * half when partly, else 0. A nudge costs a star only when it costs points.
 */
public final class Stars {

    public static final int MAX = 3;

    private static final double EPSILON = 1e-9;

    private Stars() {
    }

    public static int of(QuestionScore score, Verdict verdict) {
        double share = score.possible() > 0 ? score.earned() / score.possible() : shareOf(verdict);
        if (share >= 1 - EPSILON) {
            return 3;
        }
        if (share >= 0.5 - EPSILON) {
            return 2;
        }
        return share > EPSILON ? 1 : 0;
    }

    private static double shareOf(Verdict verdict) {
        return switch (verdict) {
            case CORRECT -> 1;
            case PARTLY -> 0.5;
            case WRONG -> 0;
        };
    }
}
