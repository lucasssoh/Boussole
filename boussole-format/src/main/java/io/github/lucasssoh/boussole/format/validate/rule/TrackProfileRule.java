package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSubject;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * P013: once the track has a plan, {@code subject.yaml} says how demanding it is ({@code level}) and
 * its field ({@code domain}), which the plan sets.
 */
public final class TrackProfileRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        CourseSubject subject = context.course().subject();
        if (context.course().syllabus().chapters().isEmpty()) {
            return;
        }
        if (subject.level().isEmpty()) {
            diagnostics.report(DiagnosticCode.P013, subject.position(), "`subject.yaml` has no `level`: write what the track takes to"
                    + " follow it, discovery, secondary, bachelor, master or research");
        }
        if (subject.domain().isEmpty()) {
            diagnostics.report(DiagnosticCode.P013, subject.position(), "`subject.yaml` has no `domain`: write its field of"
                    + " knowledge from the list of docs/FORMAT.md §2, such as information-technology");
        }
    }
}
