package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.LandmarkBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.util.Optional;
import java.util.stream.Stream;

/** The IDs a course defines, shared by the rules that look them up. */
final class CourseIds {

    private CourseIds() {
    }

    record ExerciseId(String id, SourcePosition position) {
    }

    /** Exercises and mcqs, which share one ID space in a subject. */
    static Stream<ExerciseId> exercises(Course course) {
        return course.blocks().flatMap(block -> switch (block) {
            case ExerciseBlock exercise -> Stream.of(new ExerciseId(exercise.id(), exercise.position()));
            case McqBlock mcq -> Stream.of(new ExerciseId(mcq.id(), mcq.position()));
            default -> Stream.empty();
        });
    }

    /** Landmarks, unique in a subject, since a session may name one written in another. */
    static Stream<ExerciseId> landmarks(Course course) {
        return course.blocks()
                .filter(LandmarkBlock.class::isInstance)
                .map(LandmarkBlock.class::cast)
                .map(landmark -> new ExerciseId(landmark.id(), landmark.position()));
    }

    static Optional<String> fullSessionId(Course course, String id) {
        return course.syllabus().chapters().stream()
                .flatMap(chapter -> chapter.sessions().stream().map(entry -> course.sessionId(chapter, entry).toString()))
                .filter(id::equals)
                .findFirst();
    }
}
