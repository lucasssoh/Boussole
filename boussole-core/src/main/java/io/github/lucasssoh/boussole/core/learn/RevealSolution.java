package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseType;

/**
 * Shows the solution of an exercise once the learner has tried it at least once. A free exercise
 * shows it at once: the learner reads it, then grades their own answer.
 */
public final class RevealSolution {

    private final CourseLookup lookup;
    private final ProgressRepository progress;

    public RevealSolution(ContentRepository content, ProgressRepository progress) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
    }

    public RevealedSolution reveal(ExerciseKey key) {
        Block question = lookup.question(key);
        if (!isFree(question) && !progress.exercise(key).solutionUnlocked()) {
            throw new LearningRuleException("try " + key.exercise() + " once before its solution");
        }
        return new RevealedSolution(question, CourseQuestions.solution(question));
    }

    private static boolean isFree(Block question) {
        return question instanceof ExerciseBlock exercise && exercise.type() == ExerciseType.FREE;
    }
}
