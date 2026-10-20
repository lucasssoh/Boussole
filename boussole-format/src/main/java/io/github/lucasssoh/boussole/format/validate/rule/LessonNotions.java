package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import java.util.stream.Stream;

final class LessonNotions {

    private LessonNotions() {
    }

    /** The notions of the lessons; timed sessions have none to teach. */
    static Stream<NotionPart> of(Course course) {
        return course.sessions().stream()
                .filter(loaded -> !TimedSessions.isTimed(loaded))
                .flatMap(loaded -> loaded.session().parts().stream())
                .filter(NotionPart.class::isInstance)
                .map(NotionPart.class::cast);
    }
}
