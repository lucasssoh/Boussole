package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.diagnostic.Position;

/**
 * How the learner learns best, from {@code learner.yaml}. Agents shape the sessions after it, and
 * the validator checks the sessions follow it.
 */
public record LearnerProfile(
        String background,
        Goal goal,
        int sessionMinutes,
        Explanations explanations,
        Order order,
        boolean quickCheck,
        Closing closing,
        Videos videos,
        String notes,
        Position position) {

    /** What the learner aims for: decides how much practice the course offers. */
    public enum Goal {
        PASS,
        GOOD,
        TOP,
        UNDERSTAND
    }

    public enum Explanations {
        CONCISE,
        DETAILED
    }

    /** Whether a notion shows its worked example before the learner tries, or after. */
    public enum Order {
        EXAMPLE_FIRST,
        TRY_FIRST
    }

    /** What ends a session: a quiz (checkpoint), an exercise (practice), or both. */
    public enum Closing {
        QUIZ,
        EXERCISE,
        BOTH
    }

    public enum Videos {
        NONE,
        SOME,
        MANY
    }
}
