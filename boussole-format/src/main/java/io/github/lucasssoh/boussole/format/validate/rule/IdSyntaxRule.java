package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.regex.Pattern;

/** F004: IDs are made of lowercase letters, digits and {@code -}. */
public final class IdSyntaxRule implements ValidationRule {

    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9-]*");

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        check("subject", course.subject().id(), course.subject().position(), diagnostics);
        for (SyllabusChapter chapter : course.syllabus().chapters()) {
            check("chapter", chapter.id(), chapter.position(), diagnostics);
            chapter.sessions().forEach(entry -> check("session", entry.id(), entry.position(), diagnostics));
        }
        course.lexicon().terms().forEach(term -> check("term", term.id(), term.position(), diagnostics));
        course.sessions().forEach(loaded -> loaded.session().parts().stream()
                .filter(NotionPart.class::isInstance)
                .map(NotionPart.class::cast)
                .forEach(notion -> check("notion", notion.id(), notion.position(), diagnostics)));
        CourseIds.exercises(course).forEach(exercise -> check("exercise", exercise.id(), exercise.position(), diagnostics));
    }

    private static void check(String what, String id, SourcePosition position, DiagnosticCollector diagnostics) {
        if (!id.isBlank() && !ID.matcher(id).matches()) {
            diagnostics.report(DiagnosticCode.F004, position,
                    what + " ID `" + id + "` may only hold lowercase letters, digits and `-`");
        }
    }
}
