package io.github.lucasssoh.boussole.core.learn.reward;

/**
 * How many points an answer earns: only what is right counts, and an answer never earns twice in
 * the same session. The first answer to a question earns its whole {@link QuestionScore}; a
 * session that asks it again (a warmup, a review) {@link #AGAIN} of it, once. Both are multiplied
 * by the level of the track. A session done without a single mistake earns {@link #FLAWLESS} of
 * its points more.
 */
public final class PointRules {

    public static final double AGAIN = 0.5;
    public static final double FLAWLESS = 0.10;

    private PointRules() {
    }

    /** {@code first}: the question was never answered; {@code firstHere}: never in this session. */
    public static double weight(boolean first, boolean firstHere) {
        return first ? 1 : firstHere ? AGAIN : 0;
    }

    /** The bonus of a session without a mistake, from the points it earned, rounded up last. */
    public static double flawlessBonus(double sessionPoints) {
        return Math.ceil(FLAWLESS * sessionPoints - 1e-9);
    }
}
