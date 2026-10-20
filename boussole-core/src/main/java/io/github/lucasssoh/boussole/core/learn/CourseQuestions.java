package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseHint;
import io.github.lucasssoh.boussole.format.model.block.ExerciseSolution;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.util.List;
import java.util.Optional;

/** The questions of a course, exercises and MCQs, and what they hold. */
public final class CourseQuestions {

    private CourseQuestions() {
    }

    public static Optional<Block> find(Course course, String id) {
        return course.blocks().filter(block -> id(block).filter(id::equals).isPresent()).findFirst();
    }

    /** The ID of an exercise or an MCQ; empty for any other block. */
    public static Optional<String> id(Block block) {
        return switch (block) {
            case ExerciseBlock exercise -> Optional.of(exercise.id());
            case McqBlock mcq -> Optional.of(mcq.id());
            default -> Optional.empty();
        };
    }

    public static List<ExerciseHint> hints(Block question) {
        return switch (question) {
            case ExerciseBlock exercise -> exercise.hints();
            case McqBlock mcq -> mcq.hints();
            default -> List.of();
        };
    }

    public static Optional<ExerciseSolution> solution(Block question) {
        return switch (question) {
            case ExerciseBlock exercise -> exercise.solution();
            case McqBlock mcq -> mcq.solution();
            default -> Optional.empty();
        };
    }
}
