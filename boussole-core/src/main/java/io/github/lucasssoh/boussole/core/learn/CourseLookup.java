package io.github.lucasssoh.boussole.core.learn;

import java.util.Optional;
import java.util.List;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;

/** Finds what a use case works on, or says it does not exist. */
final class CourseLookup {

    private final ContentRepository content;

    CourseLookup(ContentRepository content) {
        this.content = content;
    }

    /** The version of a track in {@code language} when it has one, else its first. */
    Course course(String subject, Optional<String> language) {
        return CourseVersions.pick(versions(subject), language);
    }

    List<Course> versions(String subject) {
        List<Course> versions = content.versions(subject);
        if (versions.isEmpty()) {
            throw new ContentNotFoundException("no subject " + subject);
        }
        return versions;
    }

    CourseSession session(Course course, String session) {
        return course.sessions().stream()
                .filter(found -> found.entry().id().equals(session))
                .findFirst()
                .orElseThrow(() -> new ContentNotFoundException(
                        "no written session " + session + " in " + course.subject().id()));
    }

    Block question(ExerciseKey key, Optional<String> language) {
        return CourseQuestions.find(course(key.subject(), language), key.exercise())
                .orElseThrow(() -> new ContentNotFoundException("no exercise " + key.exercise() + " in " + key.subject()));
    }
}
