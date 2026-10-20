package io.github.lucasssoh.boussole.core.learn.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import java.time.Duration;
import java.time.Instant;
import io.github.lucasssoh.boussole.core.learn.reward.AttemptRecord;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Where the learner's progress is kept (port). */
public interface ProgressRepository {

    ExerciseProgress exercise(ExerciseKey key);

    /** The progress of every exercise of a subject the learner has touched, by exercise ID. */
    Map<String, ExerciseProgress> exercises(String subject);

    void saveExercise(ExerciseKey key, ExerciseProgress progress, Instant at);

    /** Keeps an attempt in the history, with the session it was given in. */
    void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, Instant at);

    void saveSessionFinished(SessionKey key, Instant at, Duration spent);

    /** The IDs of the sessions of a subject finished at least once. */
    Set<String> finishedSessions(String subject);

    /** Every attempt, in every subject, oldest first. */
    List<AttemptRecord> attemptHistory();

    /** When each session was finished, in every subject. */
    List<Instant> sessionsFinishedAt();

    /** The last attempt or finished session in a subject, if the learner has started it. */
    Optional<LastActivity> lastActivity(String subject);
}
