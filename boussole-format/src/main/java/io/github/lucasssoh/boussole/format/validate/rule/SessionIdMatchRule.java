package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/** F010: the {@code id} of a session file is the one the syllabus gives it. */
public final class SessionIdMatchRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        for (LoadedSession loaded : context.course().sessions()) {
            String id = loaded.session().header().id();
            if (!id.isBlank() && !id.equals(loaded.entry().id())) {
                diagnostics.report(Code.F010, loaded.session().header().position(),
                        "the file says `id: " + id + "`, the syllabus `" + loaded.entry().id() + "`");
            }
        }
    }
}
