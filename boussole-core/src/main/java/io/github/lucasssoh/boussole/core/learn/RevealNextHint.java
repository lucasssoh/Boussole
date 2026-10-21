package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.block.ExerciseHint;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/** Reveals the next hint of an exercise, in the order of the file, and records it. */
public final class RevealNextHint {

    private final CourseLookup lookup;
    private final ProgressRepository progress;
    private final Clock clock;

    public RevealNextHint(ContentRepository content, ProgressRepository progress, Clock clock) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
        this.clock = clock;
    }

    public RevealedHint reveal(ExerciseKey key, Optional<String> language) {
        List<ExerciseHint> hints = CourseQuestions.hints(lookup.question(key, language));
        ExerciseProgress before = progress.exercise(key);
        if (before.hintsRevealed() >= hints.size()) {
            throw new LearningRuleException("no hint left for " + key.exercise());
        }
        int number = before.hintsRevealed() + 1;
        progress.saveExercise(key, before.withHintsRevealed(number), clock.instant());
        return new RevealedHint(number, hints.size(), hints.get(number - 1));
    }
}
