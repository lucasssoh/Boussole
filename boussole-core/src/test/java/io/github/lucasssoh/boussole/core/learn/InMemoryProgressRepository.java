package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class InMemoryProgressRepository implements ProgressRepository {

    private final Map<ExerciseKey, ExerciseProgress> exercises = new HashMap<>();
    private final Set<SessionKey> finished = new HashSet<>();

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
    }

    @Override
    public Set<String> finishedSessions(String subject) {
        return finished.stream().filter(key -> key.subject().equals(subject)).map(SessionKey::session).collect(Collectors.toSet());
    }
}
