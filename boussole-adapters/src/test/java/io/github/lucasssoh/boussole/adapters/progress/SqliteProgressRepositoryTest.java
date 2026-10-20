package io.github.lucasssoh.boussole.adapters.progress;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteProgressRepositoryTest {

    private static final ExerciseKey KEY = new ExerciseKey("oc", "s34-a1");
    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");

    @TempDir
    Path folder;

    private SqliteProgressRepository repository() {
        return new SqliteProgressRepository(SqliteDatabase.open(folder.resolve("data/state.db")));
    }

    @Test
    void an_untouched_exercise_has_no_progress() {
        assertThat(repository().exercise(KEY)).isEqualTo(ExerciseProgress.NONE);
    }

    @Test
    void keeps_the_progress_of_an_exercise_across_openings() {
        ExerciseProgress progress = ExerciseProgress.NONE.withAttempt(Verdict.WRONG).withAttempt(Verdict.CORRECT).withHintsRevealed(1);
        repository().saveExercise(KEY, progress, NOW);

        assertThat(repository().exercise(KEY)).isEqualTo(progress);
        assertThat(repository().exercises("oc")).containsOnlyKeys("s34-a1");
        assertThat(repository().exercises("reseaux")).isEmpty();
    }

    @Test
    void lists_the_sessions_finished() {
        repository().saveSessionFinished(new SessionKey("oc", "s34"), NOW, Duration.ofMinutes(48));
        repository().saveSessionFinished(new SessionKey("oc", "s34"), NOW.plusSeconds(86_400), Duration.ofMinutes(30));

        assertThat(repository().finishedSessions("oc")).containsExactly("s34");
    }
}
