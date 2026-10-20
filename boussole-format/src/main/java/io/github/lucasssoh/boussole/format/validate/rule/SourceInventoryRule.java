package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.source.SourceInventory;
import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.List;

/**
 * F033, F034, F035: the documents of the sources folder match {@code cursus/sources.yaml}. A
 * document arrives during the year, one changes, one is removed: the course has to follow, and
 * these warnings say where.
 */
public final class SourceInventoryRule implements ValidationRule {

    private static final String SOURCES = "cursus/sources.yaml";
    private static final String HOW = " (boussole sources lists them; the skill sources-update says what to do)";

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        List<SourceState> states = SourceInventory.compare(course.sources(), context.sources());
        if (course.sources().isEmpty()) {
            if (!states.isEmpty()) {
                diagnostics.report(DiagnosticCode.F033, course.subject().position(), states.size()
                        + (states.size() == 1 ? " document" : " documents")
                        + " in the sources folder, and no " + SOURCES + " saying what the course did with them" + HOW);
            }
            return;
        }
        for (SourceState state : states) {
            SourcePosition where = state.entry().map(entry -> entry.position()).orElse(SourcePosition.fileOnly(SOURCES));
            switch (state.state()) {
                case NEW -> diagnostics.report(DiagnosticCode.F033, where,
                        "new document `" + state.file() + "`, not yet in " + SOURCES + HOW);
                case PENDING -> diagnostics.report(DiagnosticCode.F033, where,
                        "`" + state.file() + "` waits to be taken into the course (status: pending)");
                case CHANGED -> diagnostics.report(DiagnosticCode.F034, where,
                        "`" + state.file() + "` changed since the course used it" + HOW);
                case GONE -> diagnostics.report(DiagnosticCode.F035, where,
                        "`" + state.file() + "` is listed but no longer in the sources folder");
                case USED, IGNORED -> {
                }
            }
        }
    }
}
