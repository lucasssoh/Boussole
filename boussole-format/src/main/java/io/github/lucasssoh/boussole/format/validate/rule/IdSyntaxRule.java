package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.Chapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.regex.Pattern;

/** F004: IDs are made of lowercase letters, digits and {@code -}. */
public final class IdSyntaxRule implements ValidationRule {

    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9-]*");

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Course course = context.course();
        check("subject", course.subject().id(), course.subject().position(), diagnostics);
        for (Chapter chapter : course.syllabus().chapters()) {
            check("chapter", chapter.id(), chapter.position(), diagnostics);
            chapter.sessions().forEach(entry -> check("session", entry.id(), entry.position(), diagnostics));
        }
        course.lexicon().terms().forEach(term -> check("term", term.id(), term.position(), diagnostics));
        course.sessions().forEach(loaded -> loaded.session().parts().stream()
                .filter(Notion.class::isInstance)
                .map(Notion.class::cast)
                .forEach(notion -> check("notion", notion.id(), notion.position(), diagnostics)));
        CourseIds.exercises(course).forEach(exercise -> check("exercise", exercise.id(), exercise.position(), diagnostics));
    }

    private static void check(String what, String id, Position position, Diagnostics diagnostics) {
        if (!id.isBlank() && !ID.matcher(id).matches()) {
            diagnostics.report(Code.F004, position,
                    what + " ID `" + id + "` may only hold lowercase letters, digits and `-`");
        }
    }
}
