package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.QcmBlock;
import io.github.lucasssoh.boussole.format.model.block.QuestionTimeEstimator;
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
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        Map<String, Block> questions = course.blocks()
                .filter(block -> block instanceof ExerciseBlock || block instanceof QcmBlock)
                .collect(Collectors.toMap(TimedSessionFitRule::id, Function.identity(), (first, second) -> first));
        course.sessions().stream().filter(TimedSessions::isTimed).forEach(loaded -> check(loaded, questions, diagnostics));
    }

    private static void check(CourseSession loaded, Map<String, Block> questions, DiagnosticCollector diagnostics) {
        double minutes = loaded.session().blocks().mapToDouble(block -> minutes(block, questions)).sum();
        int allowed = loaded.entry().duration();
        if (minutes < allowed * LOW_PERCENT / 100.0 || minutes > allowed * HIGH_PERCENT / 100.0) {
            diagnostics.report(DiagnosticCode.P008, loaded.session().header().position(), String.format(
                    "the questions take about %.0f min at a normal pace, the session allows %d: %s questions",
                    minutes, allowed, minutes < allowed ? "add" : "remove"));
        }
    }

    private static double minutes(Block block, Map<String, Block> questions) {
        return switch (block) {
            case ExerciseBlock exercise -> QuestionTimeEstimator.minutes(exercise);
            case QcmBlock qcm -> QuestionTimeEstimator.minutes(qcm);
            case ExerciseRefBlock ref -> questions.containsKey(ref.id()) ? minutes(questions.get(ref.id()), questions) : 0;
            default -> 0;
        };
    }

    private static String id(Block block) {
        return block instanceof ExerciseBlock exercise ? exercise.id() : ((QcmBlock) block).id();
    }
}
