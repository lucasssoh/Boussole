package io.github.lucasssoh.boussole.core.learn.progress;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/** Where the learner's progress is kept (port). */
public interface ProgressRepository {

    ExerciseProgress exercise(ExerciseKey key);

    /** The progress of every exercise of a subject the learner has touched, by exercise ID. */
    Map<String, ExerciseProgress> exercises(String subject);

    void saveExercise(ExerciseKey key, ExerciseProgress progress, Instant at);

    void saveSessionFinished(SessionKey key, Instant at, Duration spent);

    /** The IDs of the sessions of a subject finished at least once. */
    Set<String> finishedSessions(String subject);
}
