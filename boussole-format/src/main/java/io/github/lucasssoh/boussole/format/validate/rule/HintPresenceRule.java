package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SessionKind;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P004: an exercise or an MCQ offers at least one hint before its answer, except in an exam, which gives none. */
public final class HintPresenceRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().sessions().stream()
                .filter(loaded -> loaded.entry().kind() != SessionKind.EXAM)
                .flatMap(loaded -> loaded.session().blocks())
                .forEach(block -> {
                    switch (block) {
                        case ExerciseBlock exercise when exercise.hints().isEmpty() -> report(exercise.id(), block, diagnostics);
                        case McqBlock mcq when mcq.hints().isEmpty() -> report(mcq.id(), block, diagnostics);
                        default -> {
                        }
                    }
                });
    }

    private static void report(String id, Block question, DiagnosticCollector diagnostics) {
        diagnostics.report(DiagnosticCode.P004, question.position(),
                "`" + id + "` has no hint: a learner who is stuck can only guess, then read the answer");
    }
}
