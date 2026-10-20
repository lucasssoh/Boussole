package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.model.CourseSession;

/** Lessons follow the teaching contract; timed sessions (reviews, exams) only ask questions. */
final class TimedSessions {

    private TimedSessions() {
    }

    static boolean isTimed(CourseSession loaded) {
        return loaded.entry().kind().isTimed();
    }
}
