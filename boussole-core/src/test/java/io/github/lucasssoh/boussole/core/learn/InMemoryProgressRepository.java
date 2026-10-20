package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Duration;
import java.time.Instant;
import io.github.lucasssoh.boussole.core.learn.reward.AttemptRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class InMemoryProgressRepository implements ProgressRepository {

    private final Map<ExerciseKey, ExerciseProgress> exercises = new HashMap<>();
    private final Set<SessionKey> finished = new HashSet<>();
    private final Map<String, LastActivity> last = new HashMap<>();
    private final List<AttemptRecord> history = new ArrayList<>();
    private final List<Instant> finishedAt = new ArrayList<>();

    @Override
    public ExerciseProgress exercise(ExerciseKey key) {
        return exercises.getOrDefault(key, ExerciseProgress.NONE);
    }

    @Override
    public Map<String, ExerciseProgress> exercises(String subject) {
        return exercises.entrySet().stream()
                .filter(entry -> entry.getKey().subject().equals(subject))
                .collect(Collectors.toMap(entry -> entry.getKey().exercise(), Map.Entry::getValue));
    }

    @Override
    public void saveExercise(ExerciseKey key, ExerciseProgress progress, Instant at) {
        exercises.put(key, progress);
    }

    @Override
    public void saveSessionFinished(SessionKey key, Instant at, Duration spent) {
        finished.add(key);
        last.put(key.subject(), new LastActivity(at, Optional.of(key.session()), true));
        finishedAt.add(at);
    }

    @Override
    public void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, Instant at) {
        last.put(key.subject(), new LastActivity(at, session, false));
        history.add(new AttemptRecord(key.subject(), key.exercise(), at, verdict));
    }

    @Override
    public List<AttemptRecord> attemptHistory() {
        return List.copyOf(history);
    }

    @Override
    public List<Instant> sessionsFinishedAt() {
        return List.copyOf(finishedAt);
    }

    @Override
    public Optional<LastActivity> lastActivity(String subject) {
        return Optional.ofNullable(last.get(subject));
    }

    @Override
    public Set<String> finishedSessions(String subject) {
        return finished.stream().filter(key -> key.subject().equals(subject)).map(SessionKey::session).collect(Collectors.toSet());
    }
}
