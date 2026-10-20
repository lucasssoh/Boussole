package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.util.Set;

/** A course and the sessions the learner has finished in it. */
public record CourseOverview(Course course, Set<String> finishedSessions) {
}
