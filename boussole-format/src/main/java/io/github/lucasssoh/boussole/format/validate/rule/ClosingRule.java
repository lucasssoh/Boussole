package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Closing;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.part.CheckpointPart;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.PracticePart;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;

/**
 * P006: a session ends the way the learner asked in {@code learner.yaml}: a checkpoint quiz, a
 * closing exercise in the practice, or both. Without a profile, nothing is checked; timed
 * sessions end with their time.
 */
public final class ClosingRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().learner().map(LearnerProfile::closing).ifPresent(closing -> context.course().sessions().stream()
                .filter(loaded -> !TimedSessions.isTimed(loaded))
                .forEach(loaded -> check(loaded, closing, diagnostics)));
    }

    private static void check(CourseSession loaded, Closing closing, DiagnosticCollector diagnostics) {
        List<Part> parts = loaded.session().parts();
        if (closing != Closing.EXERCISE && parts.stream().noneMatch(CheckpointPart.class::isInstance)) {
            diagnostics.report(DiagnosticCode.P006, loaded.session().header().position(),
                    "the learner asked to close each session with a quiz: add a :::::checkpoint");
        }
        boolean endsWithExercise = !parts.isEmpty()
                && parts.getLast() instanceof PracticePart practice
                && practice.blocks().stream().anyMatch(ExerciseBlock.class::isInstance);
        if (closing != Closing.QUIZ && !endsWithExercise) {
            diagnostics.report(DiagnosticCode.P006, loaded.session().header().position(),
                    "the learner asked to close each session with an exercise: end with a :::::practice holding one");
        }
    }
}
