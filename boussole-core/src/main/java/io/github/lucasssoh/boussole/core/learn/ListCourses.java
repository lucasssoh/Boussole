package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import java.util.List;

/** Every course the learner follows, with what they have finished and where to resume. */
public final class ListCourses {

    private final ContentRepository content;
    private final CourseOverviews overviews;

    public ListCourses(ContentRepository content, ProgressRepository progress) {
        this.content = content;
        this.overviews = new CourseOverviews(progress);
    }

    public List<CourseOverview> list() {
        return content.courses().stream().map(overviews::of).toList();
    }
}
