package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SessionKind;

/**
 * In a lesson, a question is answered once: coming back to it later shows that answer, right or
 * wrong, so that the learner thinks before answering. A review or a mock exam can be sat again.
 */
final class AnswerOnce {

    private AnswerOnce() {
    }

    static boolean appliesTo(CourseSession session) {
        return session.entry().kind() == SessionKind.LESSON;
    }
}
