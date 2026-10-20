package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;

/**
 * How much experience each thing done brings. Trying always counts: a wrong first answer still
 * earns something, so that the learner is never afraid to answer.
 */
public final class XpRules {

    static final int FIRST_CORRECT = 10;
    static final int FIRST_PARTLY = 7;
    static final int FIRST_WRONG = 5;
    static final int AGAIN_CORRECT = 5;
    static final int AGAIN_OTHER = 2;
    public static final int SESSION_FINISHED = 20;

    private XpRules() {
    }

    /** {@code first}: the learner had never answered this question before. */
    public static int forAttempt(boolean first, Verdict verdict) {
        if (!first) {
            return verdict == Verdict.CORRECT ? AGAIN_CORRECT : AGAIN_OTHER;
        }
        return switch (verdict) {
            case CORRECT -> FIRST_CORRECT;
            case PARTLY -> FIRST_PARTLY;
            case WRONG -> FIRST_WRONG;
        };
    }
}
