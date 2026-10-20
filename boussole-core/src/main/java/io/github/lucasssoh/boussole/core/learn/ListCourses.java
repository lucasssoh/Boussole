package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import java.util.List;

/** Every course the learner follows, with what they have finished. */
public final class ListCourses {

    private final ContentRepository content;
    private final ProgressRepository progress;

    public ListCourses(ContentRepository content, ProgressRepository progress) {
        this.content = content;
        this.progress = progress;
    }

    public List<CourseOverview> list() {
        return content.courses().stream()
                .map(course -> new CourseOverview(course, progress.finishedSessions(course.subject().id())))
                .toList();
    }
}
