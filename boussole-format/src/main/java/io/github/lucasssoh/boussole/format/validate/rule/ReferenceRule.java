package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.Term;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRef;
import io.github.lucasssoh.boussole.format.model.block.Prose;
import io.github.lucasssoh.boussole.format.model.block.TermMention;
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
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Course course = context.course();
        Set<String> exercises = CourseIds.exercises(course).map(CourseIds.ExerciseId::id).collect(Collectors.toSet());
        course.blocks()
                .filter(ExerciseRef.class::isInstance)
                .map(ExerciseRef.class::cast)
                .filter(ref -> !exercises.contains(ref.id()))
                .forEach(ref -> unknown(diagnostics, ref.position(), "no exercise or qcm has the ID `" + ref.id() + "`"));

        for (Term term : course.lexicon().terms()) {
            term.see().stream()
                    .filter(id -> course.lexicon().byId(id).isEmpty())
                    .forEach(id -> unknown(diagnostics, term.position(), "term `" + term.id() + "` sees `" + id + "`, which is not in the lexicon"));
        }

        for (LoadedSession loaded : course.sessions()) {
            Position header = loaded.session().header().position();
            loaded.session().header().terms().stream()
                    .filter(id -> course.lexicon().byId(id).isEmpty())
                    .forEach(id -> unknown(diagnostics, header, "`terms` lists `" + id + "`, which is not in the lexicon"));
            loaded.session().header().prerequisites().stream()
                    .filter(id -> CourseIds.fullSessionId(course, id).isEmpty())
                    .forEach(id -> unknown(diagnostics, header, "prerequisite `" + id
                            + "` is not a session of the syllabus (write <subject>.<chapter>.<session>)"));
            loaded.session().blocks()
                    .filter(Prose.class::isInstance)
                    .flatMap(prose -> ((Prose) prose).inlines().stream())
                    .filter(TermMention.class::isInstance)
                    .map(TermMention.class::cast)
                    .filter(mention -> Terms.resolve(course, mention).isEmpty())
                    .forEach(mention -> unknown(diagnostics, mention.position(), mention.id()
                            .map(id -> "`:term` points to `" + id + "`, which is not in the lexicon")
                            .orElse("`:term[" + mention.text() + "]` matches no term or alias of the lexicon: add {id=…}")));
        }
    }

    private static void unknown(Diagnostics diagnostics, Position position, String message) {
        diagnostics.report(Code.F030, position, message);
    }
}
