package io.github.lucasssoh.boussole.core.learn.check;

/** The outcome of an attempt: partly when some gaps, pairs or choices are right and others not. */
public enum Verdict {
    CORRECT,
    PARTLY,
    WRONG;

    static Verdict of(long right, long expected) {
        if (right == expected && expected > 0) {
            return CORRECT;
        }
        return right > 0 ? PARTLY : WRONG;
    }
}
