package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.block.TermInlineDirective;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** P005: a term used in a session is listed in its front matter, which makes its vocabulary list. */
public final class DeclaredTermsRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        for (CourseSession loaded : context.course().sessions()) {
            Set<String> declared = Set.copyOf(loaded.session().header().terms());
            Set<String> reported = new HashSet<>();
            loaded.session().blocks()
                    .filter(ProseBlock.class::isInstance)
                    .flatMap(prose -> ((ProseBlock) prose).inlines().stream())
                    .filter(TermInlineDirective.class::isInstance)
                    .map(TermInlineDirective.class::cast)
                    .forEach(mention -> {
                        Optional<LexiconTerm> term = LexiconTermResolver.resolve(context.course(), mention);
                        if (term.isPresent() && !declared.contains(term.get().id()) && reported.add(term.get().id())) {
                            diagnostics.report(DiagnosticCode.P005, mention.position(), "term `" + term.get().id()
                                    + "` is used but not listed in the front matter's `terms`");
                        }
                    });
        }
    }
}
