package io.github.lucasssoh.boussole.core.learn.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import java.util.Optional;

/** What the learner has done on one exercise so far. */
public record ExerciseProgress(int attempts, Optional<Verdict> lastVerdict, boolean solved, int hintsRevealed) {

    public static final ExerciseProgress NONE = new ExerciseProgress(0, Optional.empty(), false, 0);

    public ExerciseProgress withAttempt(Verdict verdict) {
        return new ExerciseProgress(attempts + 1, Optional.of(verdict), solved || verdict == Verdict.CORRECT, hintsRevealed);
    }

    public ExerciseProgress withHintsRevealed(int count) {
        return new ExerciseProgress(attempts, lastVerdict, solved, count);
    }

    /** The solution is shown only once the learner has tried. */
    public boolean solutionUnlocked() {
        return attempts > 0;
    }
}
