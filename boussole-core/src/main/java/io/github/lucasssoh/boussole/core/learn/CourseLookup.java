package io.github.lucasssoh.boussole.core.learn;

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

    Course course(String subject) {
        return content.course(subject).orElseThrow(() -> new ContentNotFoundException("no subject " + subject));
    }

    CourseSession session(Course course, String session) {
        return course.sessions().stream()
                .filter(found -> found.entry().id().equals(session))
                .findFirst()
                .orElseThrow(() -> new ContentNotFoundException(
                        "no written session " + session + " in " + course.subject().id()));
    }

    Block question(ExerciseKey key) {
        return CourseQuestions.find(course(key.subject()), key.exercise())
                .orElseThrow(() -> new ContentNotFoundException("no exercise " + key.exercise() + " in " + key.subject()));
    }
}
