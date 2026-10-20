package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRef;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import io.github.lucasssoh.boussole.format.model.block.QuestionTime;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * P008: the questions of a timed session fill its time: their estimated times add up to between
 * {@value #LOW_PERCENT}% and {@value #HIGH_PERCENT}% of the duration allowed.
 */
public final class TimedSessionFitRule implements ValidationRule {

    static final int LOW_PERCENT = 80;
    static final int HIGH_PERCENT = 110;

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Course course = context.course();
        Map<String, Block> questions = course.blocks()
                .filter(block -> block instanceof Exercise || block instanceof Qcm)
                .collect(Collectors.toMap(TimedSessionFitRule::id, Function.identity(), (first, second) -> first));
        course.sessions().stream().filter(TimedSessions::isTimed).forEach(loaded -> check(loaded, questions, diagnostics));
    }

    private static void check(LoadedSession loaded, Map<String, Block> questions, Diagnostics diagnostics) {
        double minutes = loaded.session().blocks().mapToDouble(block -> minutes(block, questions)).sum();
        int allowed = loaded.entry().duration();
        if (minutes < allowed * LOW_PERCENT / 100.0 || minutes > allowed * HIGH_PERCENT / 100.0) {
            diagnostics.report(Code.P008, loaded.session().header().position(), String.format(
                    "the questions take about %.0f min at a normal pace, the session allows %d: %s questions",
                    minutes, allowed, minutes < allowed ? "add" : "remove"));
        }
    }

    private static double minutes(Block block, Map<String, Block> questions) {
        return switch (block) {
            case Exercise exercise -> QuestionTime.minutes(exercise);
            case Qcm qcm -> QuestionTime.minutes(qcm);
            case ExerciseRef ref -> questions.containsKey(ref.id()) ? minutes(questions.get(ref.id()), questions) : 0;
            default -> 0;
        };
    }

    private static String id(Block block) {
        return block instanceof Exercise exercise ? exercise.id() : ((Qcm) block).id();
    }
}
