package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import java.util.stream.Stream;

final class Notions {

    private Notions() {
    }

    static Stream<Notion> of(Course course) {
        return course.sessions().stream()
                .flatMap(loaded -> loaded.session().parts().stream())
                .filter(Notion.class::isInstance)
                .map(Notion.class::cast);
    }
}
