package io.github.lucasssoh.boussole.core.learn.reward;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RewardTest {

    @Test
    void a_first_answer_earns_more_than_an_answer_given_again_and_trying_always_counts() {
        assertThat(XpRules.forAttempt(true, Verdict.CORRECT)).isEqualTo(10);
        assertThat(XpRules.forAttempt(true, Verdict.WRONG)).isEqualTo(5);
        assertThat(XpRules.forAttempt(false, Verdict.CORRECT)).isEqualTo(5);
        assertThat(XpRules.forAttempt(false, Verdict.WRONG)).isEqualTo(2);
    }

    @Test
    void each_level_asks_a_hundred_more() {
        assertThat(Level.of(0)).isEqualTo(new Level(1, 0, 100));
        assertThat(Level.of(99)).isEqualTo(new Level(1, 99, 100));
        assertThat(Level.of(100)).isEqualTo(new Level(2, 0, 200));
        assertThat(Level.of(640)).isEqualTo(new Level(4, 40, 400));
    }

    @Nested
    class Stats {

        private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
        private final List<AttemptRecord> history = new ArrayList<>();
        private final List<SessionFinishedRecord> finished = new ArrayList<>();

        private StudyStats statsOn(String day) {
            Clock clock = Clock.fixed(Instant.parse(day + "T20:00:00Z"), PARIS);
            return new ReadStudyStats(new History(history, finished), RewardedSubjects.allBut("guide"), clock).read();
        }

        private void answer(String exercise, String at, Verdict verdict) {
            answer("oc", exercise, at, verdict);
        }

        private void answer(String subject, String exercise, String at, Verdict verdict) {
            history.add(new AttemptRecord(subject, exercise, Instant.parse(at), verdict));
        }

        @Test
        void counts_experience_from_the_history() {
            answer("s34-a1", "2026-10-20T08:00:00Z", Verdict.WRONG);
            answer("s34-a1", "2026-10-21T08:00:00Z", Verdict.CORRECT);
            answer("s34-a2", "2026-10-21T08:05:00Z", Verdict.CORRECT);
            finished.add(new SessionFinishedRecord("oc", Instant.parse("2026-10-21T09:00:00Z")));

            assertThat(statsOn("2026-10-21").xp()).isEqualTo(5 + 5 + 10 + 20);
        }

        @Test
        void a_subject_left_out_of_the_rewards_earns_nothing_and_keeps_no_streak() {
            answer("guide", "g1-q1", "2026-10-21T08:00:00Z", Verdict.CORRECT);
            finished.add(new SessionFinishedRecord("guide", Instant.parse("2026-10-21T09:00:00Z")));

            assertThat(statsOn("2026-10-21").xp()).isZero();
            assertThat(statsOn("2026-10-21").streakDays()).isZero();
        }

        @Test
        void a_streak_counts_the_days_in_a_row_and_survives_until_the_end_of_the_next_day() {
            answer("a", "2026-10-18T08:00:00Z", Verdict.CORRECT);
            answer("b", "2026-10-19T08:00:00Z", Verdict.CORRECT);
            answer("c", "2026-10-20T08:00:00Z", Verdict.CORRECT);

            assertThat(statsOn("2026-10-20").streakDays()).isEqualTo(3);
            assertThat(statsOn("2026-10-21").streakDays()).isEqualTo(3);
            assertThat(statsOn("2026-10-21").studiedToday()).isFalse();
            assertThat(statsOn("2026-10-22").streakDays()).isZero();
        }
    }

    private record History(List<AttemptRecord> attempts, List<SessionFinishedRecord> finished) implements ProgressRepository {
        @Override public ExerciseProgress exercise(ExerciseKey key) { return ExerciseProgress.NONE; }
        @Override public Map<String, ExerciseProgress> exercises(String subject) { return Map.of(); }
        @Override public void saveExercise(ExerciseKey key, ExerciseProgress progress, Instant at) { }
        @Override public void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, Instant at) { }
        @Override public void saveSessionFinished(SessionKey key, Instant at, Duration spent) { }
        @Override public Set<String> finishedSessions(String subject) { return Set.of(); }
        @Override public Map<String, Verdict> firstVerdicts(SessionKey session) { return Map.of(); }
        @Override public List<AttemptRecord> attemptHistory() { return attempts; }
        @Override public List<SessionFinishedRecord> sessionsFinished() { return finished; }
        @Override public Optional<LastActivity> lastActivity(String subject) { return Optional.empty(); }
    }
}
