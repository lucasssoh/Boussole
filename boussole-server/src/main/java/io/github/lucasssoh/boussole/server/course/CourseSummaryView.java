package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.core.learn.CourseOverview;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.server.guide.BundledTracks;
import java.util.Set;

/**
 * A subject in the list of subjects. {@code builtIn} marks a track shipped inside Boussole, and
 * {@code starter} the one a new Boussole opens on, shown beside the session to resume.
 */
record CourseSummaryView(
        String id,
        String name,
        String lang,
        String description,
        int planned,
        int written,
        int finished,
        String lastStudied,
        ResumeView resume,
        boolean builtIn,
        boolean starter) {

    static CourseSummaryView of(CourseOverview overview, Set<String> builtIn) {
        Course course = overview.course();
        return new CourseSummaryView(
                course.subject().id(),
                course.subject().name(),
                course.subject().lang(),
                course.subject().description().orElse(null),
                course.syllabus().chapters().stream().map(SyllabusChapter::sessions).mapToInt(java.util.List::size).sum(),
                course.sessions().size(),
                overview.finishedSessions().size(),
                overview.lastStudied().map(Object::toString).orElse(null),
                ResumeView.of(overview).orElse(null),
                builtIn.contains(course.subject().id()),
                course.subject().id().equals(BundledTracks.STARTER));
    }
}
