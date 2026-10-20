package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.FillGap;

/**
 * How long a question takes at a normal pace: choosing an answer is quicker than building one.
 * The {@code minutes} attribute of an exercise or a mcq overrides the estimate.
 */
public final class QuestionTimeEstimator {

    static final double SINGLE_CHOICE = 1;
    static final double MULTIPLE_CHOICE = 1.5;
    static final double ARRANGE = 2;
    static final double BUILD = 3;
    static final double FREE = 8;
    static final double CHOSEN_GAP = 0.5;
    static final double TYPED_GAP = 1;
    static final double MIN_FILL = 1;

    private QuestionTimeEstimator() {
    }

    public static double minutes(ExerciseBlock exercise) {
        if (exercise.minutes().isPresent()) {
            return exercise.minutes().getAsInt();
        }
        if (exercise.answer().isPresent() && exercise.answer().get() instanceof FillAnswer fill) {
            return Math.max(MIN_FILL, fill.gaps().stream().mapToDouble(QuestionTimeEstimator::minutes).sum());
        }
        return switch (exercise.type()) {
            case ORDER, MATCH -> ARRANGE;
            case NUMERIC, TEXT, FILL -> BUILD;
            case FREE -> FREE;
        };
    }

    public static double minutes(McqBlock mcq) {
        if (mcq.minutes().isPresent()) {
            return mcq.minutes().getAsInt();
        }
        return mcq.multiple() ? MULTIPLE_CHOICE : SINGLE_CHOICE;
    }

    private static double minutes(FillGap gap) {
        return gap.expected().isChosen() ? CHOSEN_GAP : TYPED_GAP;
    }
}
