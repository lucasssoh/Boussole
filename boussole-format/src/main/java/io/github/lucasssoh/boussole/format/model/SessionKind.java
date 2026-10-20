package io.github.lucasssoh.boussole.format.model;

/**
 * What a session is. A lesson's duration is an estimate of its content at a normal pace; a
 * review or an exam is timed, and its duration is the time allowed.
 */
public enum SessionKind {
    LESSON(15, 120),
    REVIEW(5, 120),
    EXAM(5, 240);

    private final int minDuration;
    private final int maxDuration;

    SessionKind(int minDuration, int maxDuration) {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int minDuration() {
        return minDuration;
    }

    public int maxDuration() {
        return maxDuration;
    }

    public boolean isTimed() {
        return this != LESSON;
    }
}
