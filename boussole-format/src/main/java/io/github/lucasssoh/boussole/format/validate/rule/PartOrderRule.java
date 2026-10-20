package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.model.part.PracticePart;
import io.github.lucasssoh.boussole.format.model.part.WarmupPart;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;

/** P003: the warmup opens a session and the practice closes it. */
public final class PartOrderRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        for (CourseSession loaded : context.course().sessions()) {
            List<Part> parts = loaded.session().parts();
            for (int index = 0; index < parts.size(); index++) {
                Part part = parts.get(index);
                if (part instanceof WarmupPart && index != 0) {
                    diagnostics.report(DiagnosticCode.P003, part.position(), "the warmup comes first in a session");
                }
                if (part instanceof PracticePart && index != parts.size() - 1) {
                    diagnostics.report(DiagnosticCode.P003, part.position(), "the practice comes last in a session");
                }
            }
        }
    }
}
