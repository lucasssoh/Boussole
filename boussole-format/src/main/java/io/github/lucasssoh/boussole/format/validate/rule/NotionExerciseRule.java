package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P001: each notion ends with something to do, an exercise or a mcq. */
public final class NotionExerciseRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        LessonNotions.of(context.course())
                .filter(notion -> notion.blocks().stream().noneMatch(block -> block instanceof ExerciseBlock || block instanceof McqBlock))
                .forEach(notion -> diagnostics.report(DiagnosticCode.P001, notion.position(),
                        "notion `" + notion.id() + "` has no exercise or MCQ for the learner to try"));
    }
}
