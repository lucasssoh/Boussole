package io.github.lucasssoh.boussole.adapters.progress;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.reward.AttemptRecord;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
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
    void knows_the_last_thing_done_in_a_subject() {
        assertThat(repository().lastActivity("oc")).isEmpty();

        repository().recordAttempt(KEY, Optional.of("s39"), Verdict.WRONG, NOW);
        assertThat(repository().lastActivity("oc")).contains(new LastActivity(NOW, Optional.of("s39"), false));

        repository().saveSessionFinished(new SessionKey("oc", "s39"), NOW.plusSeconds(60), Duration.ofMinutes(40));
        assertThat(repository().lastActivity("oc")).contains(new LastActivity(NOW.plusSeconds(60), Optional.of("s39"), true));
    }

    @Test
    void keeps_the_history_of_attempts_and_finished_sessions() {
        repository().recordAttempt(KEY, Optional.empty(), Verdict.WRONG, NOW);
        repository().recordAttempt(KEY, Optional.empty(), Verdict.CORRECT, NOW.plusSeconds(10));
        repository().saveSessionFinished(new SessionKey("oc", "s34"), NOW.plusSeconds(60), Duration.ofMinutes(40));

        assertThat(repository().attemptHistory()).extracting(AttemptRecord::verdict).containsExactly(Verdict.WRONG, Verdict.CORRECT);
        assertThat(repository().sessionsFinishedAt()).containsExactly(NOW.plusSeconds(60));
    }

    @Test
    void lists_the_sessions_finished() {
        repository().saveSessionFinished(new SessionKey("oc", "s34"), NOW, Duration.ofMinutes(48));
        repository().saveSessionFinished(new SessionKey("oc", "s34"), NOW.plusSeconds(86_400), Duration.ofMinutes(30));

        assertThat(repository().finishedSessions("oc")).containsExactly("s34");
    }
}
