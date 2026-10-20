package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.model.Chapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * F003: chapters and terms are unique in the subject, sessions in their chapter, notions in
 * their session, and exercises in the subject.
 */
public final class DuplicateIdRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, Diagnostics diagnostics) {
        Course course = context.course();
        IdSeen chapters = new IdSeen("chapter", diagnostics);
        for (Chapter chapter : course.syllabus().chapters()) {
            chapters.add(chapter.id(), chapter.position());
            IdSeen sessions = new IdSeen("session", diagnostics);
            chapter.sessions().forEach(entry -> sessions.add(entry.id(), entry.position()));
        }
        IdSeen terms = new IdSeen("term", diagnostics);
        course.lexicon().terms().forEach(term -> terms.add(term.id(), term.position()));
        for (LoadedSession loaded : course.sessions()) {
            IdSeen notions = new IdSeen("notion", diagnostics);
            loaded.session().parts().stream()
                    .filter(Notion.class::isInstance)
                    .map(Notion.class::cast)
                    .forEach(notion -> notions.add(notion.id(), notion.position()));
        }
        IdSeen exercises = new IdSeen("exercise", diagnostics);
        CourseIds.exercises(course).forEach(exercise -> exercises.add(exercise.id(), exercise.position()));
    }
}
