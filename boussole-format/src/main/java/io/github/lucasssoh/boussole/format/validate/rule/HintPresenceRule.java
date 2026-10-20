package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SessionKind;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P004: an exercise offers at least one hint before its solution, except in an exam, which gives none. */
public final class HintPresenceRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().sessions().stream()
                .filter(loaded -> loaded.entry().kind() != SessionKind.EXAM)
                .flatMap(loaded -> loaded.session().blocks())
                .filter(ExerciseBlock.class::isInstance)
                .map(ExerciseBlock.class::cast)
                .filter(exercise -> exercise.hints().isEmpty())
                .forEach(exercise -> diagnostics.report(DiagnosticCode.P004, exercise.position(),
                        "exercise `" + exercise.id() + "` has no hint: the learner goes straight to the solution"));
    }
}
