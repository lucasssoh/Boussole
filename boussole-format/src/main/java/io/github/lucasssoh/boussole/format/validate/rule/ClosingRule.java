package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.LearnerProfile;
import io.github.lucasssoh.boussole.format.model.LearnerProfile.Closing;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.part.Checkpoint;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.Practice;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;

/**
 * P006: a session ends the way the learner asked in {@code learner.yaml}: a checkpoint quiz, a
 * closing exercise in the practice, or both. Without a profile, nothing is checked.
 */
public final class ClosingRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        context.course().learner().map(LearnerProfile::closing).ifPresent(closing -> context.course().sessions()
                .forEach(loaded -> check(loaded, closing, diagnostics)));
    }

    private static void check(LoadedSession loaded, Closing closing, Diagnostics diagnostics) {
        List<Part> parts = loaded.session().parts();
        if (closing != Closing.EXERCISE && parts.stream().noneMatch(Checkpoint.class::isInstance)) {
            diagnostics.report(Code.P006, loaded.session().header().position(),
                    "the learner asked to close each session with a quiz: add a :::::checkpoint");
        }
        boolean endsWithExercise = !parts.isEmpty()
                && parts.getLast() instanceof Practice practice
                && practice.blocks().stream().anyMatch(Exercise.class::isInstance);
        if (closing != Closing.QUIZ && !endsWithExercise) {
            diagnostics.report(Code.P006, loaded.session().header().position(),
                    "the learner asked to close each session with an exercise: end with a :::::practice holding one");
        }
    }
}
