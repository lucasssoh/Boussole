package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;
import java.util.Set;

/**
 * F045: a session written from a plan that gave its prerequisites lists the same ones in its
 * front matter, so that the plan and the session do not tell two stories.
 */
public final class PlannedPrerequisitesRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        for (CourseSession session : context.course().sessions()) {
            List<String> planned = session.entry().prerequisites();
            List<String> written = session.session().header().prerequisites();
            if (!planned.isEmpty() && !Set.copyOf(planned).equals(Set.copyOf(written))) {
                diagnostics.report(DiagnosticCode.F045, session.session().header().position(), "`prerequisites` of `"
                        + session.entry().id() + "` are " + listed(written) + " but the syllabus plans " + listed(planned)
                        + ": write `prerequisites: [" + String.join(", ", planned) + "]` in the front matter, or change both");
            }
        }
    }

    private static String listed(List<String> ids) {
        return ids.isEmpty() ? "none" : "`" + String.join("`, `", ids) + "`";
    }
}
