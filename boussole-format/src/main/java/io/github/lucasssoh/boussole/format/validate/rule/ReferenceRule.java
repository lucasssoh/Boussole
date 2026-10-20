package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.LexiconTerm;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.block.TermInlineDirective;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * F030: every reference points to something that exists: {@code ref} to an exercise,
 * {@code term} to the lexicon, {@code terms} and {@code prerequisites} of a front matter, and
 * {@code see} of a term.
 */
public final class ReferenceRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        Set<String> exercises = CourseIds.exercises(course).map(CourseIds.ExerciseId::id).collect(Collectors.toSet());
        course.blocks()
                .filter(ExerciseRefBlock.class::isInstance)
                .map(ExerciseRefBlock.class::cast)
                .filter(ref -> !exercises.contains(ref.id()))
                .forEach(ref -> unknown(diagnostics, ref.position(), "no exercise or MCQ has the ID `" + ref.id() + "`"));

        for (LexiconTerm term : course.lexicon().terms()) {
            term.see().stream()
                    .filter(id -> course.lexicon().byId(id).isEmpty())
                    .forEach(id -> unknown(diagnostics, term.position(), "term `" + term.id() + "` sees `" + id + "`, which is not in the lexicon"));
        }

        for (CourseSession loaded : course.sessions()) {
            SourcePosition header = loaded.session().header().position();
            loaded.session().header().terms().stream()
                    .filter(id -> course.lexicon().byId(id).isEmpty())
                    .forEach(id -> unknown(diagnostics, header, "`terms` lists `" + id + "`, which is not in the lexicon"));
            loaded.session().header().prerequisites().stream()
                    .filter(id -> CourseIds.fullSessionId(course, id).isEmpty())
                    .forEach(id -> unknown(diagnostics, header, "prerequisite `" + id
                            + "` is not a session of the syllabus (write <subject>.<chapter>.<session>)"));
            loaded.session().blocks()
                    .filter(ProseBlock.class::isInstance)
                    .flatMap(prose -> ((ProseBlock) prose).inlines().stream())
                    .filter(TermInlineDirective.class::isInstance)
                    .map(TermInlineDirective.class::cast)
                    .filter(mention -> LexiconTermResolver.resolve(course, mention).isEmpty())
                    .forEach(mention -> unknown(diagnostics, mention.position(), mention.id()
                            .map(id -> "`:term` points to `" + id + "`, which is not in the lexicon")
                            .orElse("`:term[" + mention.text() + "]` matches no term or alias of the lexicon: add {id=…}")));
        }
    }

    private static void unknown(DiagnosticCollector diagnostics, SourcePosition position, String message) {
        diagnostics.report(DiagnosticCode.F030, position, message);
    }
}
