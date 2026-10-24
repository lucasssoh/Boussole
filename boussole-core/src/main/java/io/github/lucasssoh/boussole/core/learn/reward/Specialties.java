package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * What the top of the culture asks besides points: specialties at {@link #LEVEL} or above. Sage
 * asks for three; Grand Sage for three besides {@code critical-thinking}, and that one too.
 */
public record Specialties(int atLevel, int besideCriticalThinking, boolean criticalThinking) {

    /** The specialty tier that counts: Specialist, then Authority and Luminary. */
    public static final String LEVEL = "specialist";
    public static final int FOR_SAGE = 3;

    public static final Specialties NONE = new Specialties(0, 0, false);

    /** Whether a culture tier, Sage or Grand Sage, is open to the learner. */
    boolean open(Ladder ladder, int tier) {
        if (tier == ladder.top()) {
            return besideCriticalThinking >= FOR_SAGE && criticalThinking;
        }
        return tier < ladder.top() - 1 || atLevel >= FOR_SAGE;
    }
}
