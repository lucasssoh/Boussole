package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.BlockWalker;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.WarmupPart;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F043: a hook question, a guess on what the session is about to teach, belongs in its warmup. */
public final class HookPlaceRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        for (CourseSession loaded : context.course().sessions()) {
            for (Part part : loaded.session().parts()) {
                if (part instanceof WarmupPart) {
                    continue;
                }
                BlockWalker.walk(part.blocks()).forEach(block -> {
                    switch (block) {
                        case McqBlock mcq when mcq.hook() -> outside(diagnostics, mcq.id(), mcq.position());
                        case ExerciseBlock exercise when exercise.hook() -> outside(diagnostics, exercise.id(), exercise.position());
                        default -> { }
                    }
                });
            }
        }
    }

    private static void outside(DiagnosticCollector diagnostics, String id, SourcePosition position) {
        diagnostics.report(DiagnosticCode.F043, position, "`" + id + "` is a hook (`hook=true`), which belongs in the warmup:"
                + " move it there, or remove `hook=true` so that it earns points");
    }
}
