package io.github.lucasssoh.boussole.core.learn;

/** An action the learner may not take yet: the solution before any attempt, a hint when none is left. */
public final class LearningRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LearningRuleException(String message) {
        super(message);
    }
}
