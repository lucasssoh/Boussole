package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;

/** One course: its syllabus, its lexicon, and what the learner has finished. */
public final class OpenCourse {

    private final CourseLookup lookup;
    private final ProgressRepository progress;

    public OpenCourse(ContentRepository content, ProgressRepository progress) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
    }

    public CourseOverview open(String subject) {
        return new CourseOverview(lookup.course(subject), progress.finishedSessions(subject));
    }
}
