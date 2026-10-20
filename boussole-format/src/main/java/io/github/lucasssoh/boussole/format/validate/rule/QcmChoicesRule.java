package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.block.QcmChoice;
import io.github.lucasssoh.boussole.format.model.block.QcmBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F022: a qcm has at least two choices, one ticked, or several with {@code multiple=true}. */
public final class QcmChoicesRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().blocks()
                .filter(QcmBlock.class::isInstance)
                .map(QcmBlock.class::cast)
                .forEach(qcm -> check(qcm, diagnostics));
    }

    private static void check(QcmBlock qcm, DiagnosticCollector diagnostics) {
        long ticked = qcm.choices().stream().filter(QcmChoice::correct).count();
        if (qcm.choices().size() < 2) {
            diagnostics.report(DiagnosticCode.F022, qcm.position(), "qcm `" + qcm.id() + "` needs at least two choices `- [ ]`");
        } else if (ticked == 0) {
            diagnostics.report(DiagnosticCode.F022, qcm.position(), "qcm `" + qcm.id() + "` has no ticked choice `- [x]`");
        } else if (ticked > 1 && !qcm.multiple()) {
            diagnostics.report(DiagnosticCode.F022, qcm.position(), "qcm `" + qcm.id() + "` has " + ticked
                    + " ticked choices: add `multiple=true`, or tick only one");
        }
    }
}
