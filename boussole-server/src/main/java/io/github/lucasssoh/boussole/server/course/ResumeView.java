package io.github.lucasssoh.boussole.server.course;

import io.github.lucasssoh.boussole.core.learn.CourseOverview;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import java.util.Locale;
import java.util.Optional;

/** The session to resume in a subject. */
record ResumeView(String id, String title, String kind, int duration, String chapterTitle) {

    static Optional<ResumeView> of(CourseOverview overview) {
        return overview.resume().flatMap(id -> overview.course().sessions().stream()
                .filter(session -> session.entry().id().equals(id))
                .findFirst()
                .map(ResumeView::of));
    }

    private static ResumeView of(CourseSession session) {
        return new ResumeView(session.entry().id(), session.entry().title(),
                session.entry().kind().name().toLowerCase(Locale.ROOT), session.entry().duration(), session.chapter().title());
    }
}
