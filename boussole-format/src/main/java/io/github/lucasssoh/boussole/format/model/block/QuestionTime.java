package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Gap;

/**
 * How long a question takes at a normal pace: choosing an answer is quicker than building one.
 * The {@code minutes} attribute of an exercise or a qcm overrides the estimate.
 */
public final class QuestionTime {

    static final double SINGLE_CHOICE = 1;
    static final double MULTIPLE_CHOICE = 1.5;
    static final double ARRANGE = 2;
    static final double BUILD = 3;
    static final double FREE = 8;
    static final double CHOSEN_GAP = 0.5;
    static final double TYPED_GAP = 1;
    static final double MIN_FILL = 1;

    private QuestionTime() {
    }

    public static double minutes(Exercise exercise) {
        if (exercise.minutes().isPresent()) {
            return exercise.minutes().getAsInt();
        }
        if (exercise.answer().isPresent() && exercise.answer().get() instanceof FillAnswer fill) {
            return Math.max(MIN_FILL, fill.gaps().stream().mapToDouble(QuestionTime::minutes).sum());
        }
        return switch (exercise.type()) {
            case ORDER, MATCH -> ARRANGE;
            case NUMERIC, TEXT, FILL -> BUILD;
            case FREE -> FREE;
        };
    }

    public static double minutes(Qcm qcm) {
        if (qcm.minutes().isPresent()) {
            return qcm.minutes().getAsInt();
        }
        return qcm.multiple() ? MULTIPLE_CHOICE : SINGLE_CHOICE;
    }

    private static double minutes(Gap gap) {
        return gap.expected().isChosen() ? CHOSEN_GAP : TYPED_GAP;
    }
}
