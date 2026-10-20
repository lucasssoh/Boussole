package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;

/**
 * F003: chapters and terms are unique in the subject, sessions in their chapter, notions in
 * their session, and exercises in the subject.
 */
public final class DuplicateIdRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        DuplicateIdTracker chapters = new DuplicateIdTracker("chapter", diagnostics);
        for (SyllabusChapter chapter : course.syllabus().chapters()) {
            chapters.add(chapter.id(), chapter.position());
            DuplicateIdTracker sessions = new DuplicateIdTracker("session", diagnostics);
            chapter.sessions().forEach(entry -> sessions.add(entry.id(), entry.position()));
        }
        DuplicateIdTracker terms = new DuplicateIdTracker("term", diagnostics);
        course.lexicon().terms().forEach(term -> terms.add(term.id(), term.position()));
        for (CourseSession loaded : course.sessions()) {
            DuplicateIdTracker notions = new DuplicateIdTracker("notion", diagnostics);
            loaded.session().parts().stream()
                    .filter(NotionPart.class::isInstance)
                    .map(NotionPart.class::cast)
                    .forEach(notion -> notions.add(notion.id(), notion.position()));
        }
        DuplicateIdTracker exercises = new DuplicateIdTracker("exercise", diagnostics);
        CourseIds.exercises(course).forEach(exercise -> exercises.add(exercise.id(), exercise.position()));
    }
}
