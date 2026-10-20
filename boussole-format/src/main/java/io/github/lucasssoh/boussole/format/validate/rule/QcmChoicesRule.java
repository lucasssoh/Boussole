package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.block.Choice;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F022: a qcm has at least two choices, one ticked, or several with {@code multiple=true}. */
public final class QcmChoicesRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        context.course().blocks()
                .filter(Qcm.class::isInstance)
                .map(Qcm.class::cast)
                .forEach(qcm -> check(qcm, diagnostics));
    }

    private static void check(Qcm qcm, Diagnostics diagnostics) {
        long ticked = qcm.choices().stream().filter(Choice::correct).count();
        if (qcm.choices().size() < 2) {
            diagnostics.report(Code.F022, qcm.position(), "qcm `" + qcm.id() + "` needs at least two choices `- [ ]`");
        } else if (ticked == 0) {
            diagnostics.report(Code.F022, qcm.position(), "qcm `" + qcm.id() + "` has no ticked choice `- [x]`");
        } else if (ticked > 1 && !qcm.multiple()) {
            diagnostics.report(Code.F022, qcm.position(), "qcm `" + qcm.id() + "` has " + ticked
                    + " ticked choices: add `multiple=true`, or tick only one");
        }
    }
}
