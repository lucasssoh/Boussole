package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F022: a mcq has at least two choices, one ticked, or several with {@code multiple=true}. */
public final class McqChoicesRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().blocks()
                .filter(McqBlock.class::isInstance)
                .map(McqBlock.class::cast)
                .forEach(mcq -> check(mcq, diagnostics));
    }

    private static void check(McqBlock mcq, DiagnosticCollector diagnostics) {
        long ticked = mcq.choices().stream().filter(McqChoice::correct).count();
        if (mcq.choices().size() < 2) {
            diagnostics.report(DiagnosticCode.F022, mcq.position(), "MCQ `" + mcq.id() + "` needs at least two choices `- [ ]`");
        } else if (ticked == 0) {
            diagnostics.report(DiagnosticCode.F022, mcq.position(), "MCQ `" + mcq.id() + "` has no ticked choice `- [x]`");
        } else if (ticked > 1 && !mcq.multiple()) {
            diagnostics.report(DiagnosticCode.F022, mcq.position(), "MCQ `" + mcq.id() + "` has " + ticked
                    + " ticked choices: add `multiple=true`, or tick only one");
        }
    }
}
