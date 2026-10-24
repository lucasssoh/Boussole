package io.github.lucasssoh.boussole.core.learn.reward;

import java.util.List;
import java.util.Locale;

/**
 * Something done, kept for good: each achievement has levels (I, II, III…), reached at the values
 * of {@link #thresholds()}. They earn no points.
 */
public enum Achievement {
    /** The guide « Getting started » finished. */
    COMPASS_IN_HAND(1),
    /** A first session finished. */
    THE_FIRST_STEP(1),
    /** Days of study in a row, at best. */
    DROP_BY_DROP(3, 7, 21, 60, 365),
    /** Sessions finished. */
    THE_PATH_IS_MADE_BY_WALKING(10, 50, 100, 500),
    /** Sessions finished without a mistake. */
    WITHOUT_A_BLOT(1, 10, 50, 200),
    /** Questions right at the first answer. */
    AT_THE_FIRST_STROKE(100, 500, 1000, 5000),
    /** Fills right in every gap. */
    THE_RIGHT_WORD(25, 100, 500),
    /** Multiple-choice MCQs right in every box. */
    NOTHING_ESCAPES_ME(25, 100, 500),
    /** Sessions finished without opening a nudge. */
    ALONE_AT_THE_HELM(1, 25, 100),
    /** Questions asked again in another session, right. */
    ARIADNES_THREAD(50, 250, 1000),
    /** Questions missed at first, right when asked again in another session. */
    TWENTY_TIMES_ON_THE_LOOM(10, 50, 200),
    /** Quick reviews finished without a mistake. */
    THE_MADELEINE(1, 10, 50),
    /** Tracks finished. */
    ITHACA(1, 5, 10, 25, 100),
    /** Tracks finished with at least nine tenths of their points. */
    THE_GOLDEN_FLEECE(1, 5, 25),
    /** The best mock exam, in percent of its points. */
    TRIAL_BY_FIRE(50, 80, 95),
    /** Fields with a track finished. */
    THE_HONEST_MAN(2, 3, 5, 8),
    /** Master tracks finished. */
    ON_THE_SHOULDERS_OF_GIANTS(1, 5, 20),
    /** Research tracks finished. */
    TERRA_INCOGNITA(1, 5),
    /** A discovery track and a research track finished within a year. */
    FROM_STREAM_TO_OCEAN(1);

    private final List<Integer> thresholds;

    Achievement(Integer... thresholds) {
        this.thresholds = List.of(thresholds);
    }

    public List<Integer> thresholds() {
        return thresholds;
    }

    /** As the interface names it: {@code drop-by-drop}. */
    public String id() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** How many levels a value reaches. */
    public int levelOf(int value) {
        return (int) thresholds.stream().filter(threshold -> value >= threshold).count();
    }
}
