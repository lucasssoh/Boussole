package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import java.util.Optional;
import java.util.stream.Stream;

/** The IDs a course defines, shared by the rules that look them up. */
final class CourseIds {

    private CourseIds() {
    }

    record ExerciseId(String id, Position position) {
    }

    /** Exercises and qcms, which share one ID space in a subject. */
    static Stream<ExerciseId> exercises(Course course) {
        return course.blocks().flatMap(block -> switch (block) {
            case Exercise exercise -> Stream.of(new ExerciseId(exercise.id(), exercise.position()));
            case Qcm qcm -> Stream.of(new ExerciseId(qcm.id(), qcm.position()));
            default -> Stream.empty();
        });
    }

    static Optional<String> fullSessionId(Course course, String id) {
        return course.syllabus().chapters().stream()
                .flatMap(chapter -> chapter.sessions().stream().map(entry -> course.sessionId(chapter, entry).toString()))
                .filter(id::equals)
                .findFirst();
    }
}
