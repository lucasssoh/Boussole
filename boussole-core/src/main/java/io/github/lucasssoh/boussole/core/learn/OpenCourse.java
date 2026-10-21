package io.github.lucasssoh.boussole.core.learn;

import java.util.Optional;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;

/** One course: its syllabus, its lexicon, and what the learner has finished. */
public final class OpenCourse {

    private final CourseLookup lookup;
    private final CourseOverviews overviews;

    public OpenCourse(ContentRepository content, ProgressRepository progress) {
        this.lookup = new CourseLookup(content);
        this.overviews = new CourseOverviews(progress);
    }

    /** In {@code language} when the track exists in it, else in its first language. */
    public CourseOverview open(String subject, Optional<String> language) {
        return overviews.of(lookup.course(subject, language), CourseVersions.languages(lookup.versions(subject)));
    }
}
