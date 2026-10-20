package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.core.learn.CourseOverview;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.SyllabusChapter;
import io.github.lucasssoh.boussole.server.guide.BundledGuide;

/** A subject in the list of subjects; {@code guide} marks the guide built into Boussole. */
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
        boolean guide) {

    static CourseSummaryView of(CourseOverview overview) {
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
                course.subject().id().equals(BundledGuide.SUBJECT));
    }
}
