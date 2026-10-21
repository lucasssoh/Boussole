package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SourceStatus;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * F039: a document taken from the web says under which licence it is shared, so that the learner
 * knows what they may keep, print or pass on.
 */
public final class SourceProvenanceRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        context.course().sources().stream()
                .flatMap(sources -> sources.entries().stream())
                .filter(entry -> entry.status() != SourceStatus.IGNORED)
                .filter(entry -> entry.provenance().url().isPresent() && entry.provenance().licence().isEmpty())
                .forEach(entry -> diagnostics.report(DiagnosticCode.F039, entry.position(), "`" + entry.file() + "` comes from "
                        + entry.provenance().url().get() + ": add its `licence` (CC BY-SA 4.0, public domain, all rights reserved…), "
                        + "as the page says"));
    }
}
