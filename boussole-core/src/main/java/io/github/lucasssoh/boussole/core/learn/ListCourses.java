package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import java.util.List;

/**
 * Every track the learner follows, once each whatever its number of languages, with what they
 * have finished and where to resume.
 */
public final class ListCourses {

    private final ContentRepository content;
    private final CourseOverviews overviews;

    public ListCourses(ContentRepository content, ProgressRepository progress) {
        this.content = content;
        this.overviews = new CourseOverviews(progress);
    }

    /** {@code language}: the one to show a track in when it exists in several, such as the interface's. */
    public List<CourseOverview> list(Optional<String> language) {
        Map<String, List<Course>> tracks = content.courses().stream()
                .collect(Collectors.groupingBy(course -> course.subject().id(), LinkedHashMap::new, Collectors.toList()));
        return tracks.values().stream()
                .map(versions -> overviews.of(CourseVersions.pick(versions, language), CourseVersions.languages(versions)))
                .toList();
    }
}
