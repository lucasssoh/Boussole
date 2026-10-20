package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.Term;
import io.github.lucasssoh.boussole.format.model.block.Prose;
import io.github.lucasssoh.boussole.format.model.block.TermMention;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** P005: a term used in a session is listed in its front matter, which makes its vocabulary list. */
public final class DeclaredTermsRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        for (LoadedSession loaded : context.course().sessions()) {
            Set<String> declared = Set.copyOf(loaded.session().header().terms());
            Set<String> reported = new HashSet<>();
            loaded.session().blocks()
                    .filter(Prose.class::isInstance)
                    .flatMap(prose -> ((Prose) prose).inlines().stream())
                    .filter(TermMention.class::isInstance)
                    .map(TermMention.class::cast)
                    .forEach(mention -> {
                        Optional<Term> term = Terms.resolve(context.course(), mention);
                        if (term.isPresent() && !declared.contains(term.get().id()) && reported.add(term.get().id())) {
                            diagnostics.report(Code.P005, mention.position(), "term `" + term.get().id()
                                    + "` is used but not listed in the front matter's `terms`");
                        }
                    });
        }
    }
}
