package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** P012: a session says who wrote it, the AI model by its exact ID or the person, so that the learner knows. */
public final class AuthorshipRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().sessions().stream()
                .filter(loaded -> loaded.session().header().writtenBy().isEmpty())
                .forEach(loaded -> diagnostics.report(DiagnosticCode.P012, loaded.session().header().position(),
                        "session `" + loaded.entry().id() + "` does not say who wrote it: add `written_by` with your `model` "
                                + "(its exact ID) and `tool`, or a `name`, and the `date`"));
    }
}
