package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.block.Example;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.model.part.Part;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.Locale;

/** P009: a timed session asks questions; it teaches no notion and shows no worked example. */
public final class TimedSessionContentRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        context.course().sessions().stream().filter(TimedSessions::isTimed).forEach(loaded -> check(loaded, diagnostics));
    }

    private static void check(LoadedSession loaded, Diagnostics diagnostics) {
        for (Part part : loaded.session().parts()) {
            if (part instanceof Notion notion) {
                diagnostics.report(Code.P009, notion.position(), article(loaded) + " asks questions: notion `"
                        + notion.id() + "` belongs in a lesson");
            }
        }
        loaded.session().blocks().filter(Example.class::isInstance).forEach(example -> diagnostics.report(
                Code.P009, example.position(), article(loaded) + " shows no worked example; its solutions come after the answers"));
    }

    private static String article(LoadedSession loaded) {
        String kind = kind(loaded);
        return (kind.startsWith("e") ? "an " : "a ") + kind;
    }

    private static String kind(LoadedSession loaded) {
        return loaded.entry().kind().name().toLowerCase(Locale.ROOT);
    }
}
