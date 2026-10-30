package io.github.lucasssoh.boussole.core.learn.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import java.time.Duration;
import java.time.Instant;
import io.github.lucasssoh.boussole.core.learn.reward.AttemptRecord;
import io.github.lucasssoh.boussole.core.learn.reward.SessionFinishedRecord;
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

    /** Keeps an attempt in the history, with the session it was given in, the points and the stars it earned. */
    void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, double points, int stars, Instant at);

    /** Keeps a finished session, with the bonus it earned when done without a mistake. */
    void saveSessionFinished(SessionKey key, Instant at, Duration spent, double bonus);

    /** The IDs of the sessions of a subject finished at least once. */
    Set<String> finishedSessions(String subject);

    /** The first attempt on each exercise answered in a session, by exercise ID. */
    default Map<String, FirstAnswer> firstAnswers(SessionKey session) {
        return firstAnswers(session.subject()).getOrDefault(session.session(), Map.of());
    }

    /** The first attempt on each exercise answered in each session of a subject, by session ID then exercise ID. */
    Map<String, Map<String, FirstAnswer>> firstAnswers(String subject);

    /** Every attempt, in every subject, oldest first. */
    List<AttemptRecord> attemptHistory();

    /** Each session finished, in every subject, oldest first. */
    List<SessionFinishedRecord> sessionsFinished();

    /** The last attempt or finished session in a subject, if the learner has started it. */
    Optional<LastActivity> lastActivity(String subject);
}
