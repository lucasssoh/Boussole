package io.github.lucasssoh.boussole.core.learn.reward;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.Domain;
import java.nio.file.Path;
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
    void only_the_first_answer_and_the_first_one_of_another_session_earn() {
        assertThat(PointRules.weight(true, true)).isEqualTo(1);
        assertThat(PointRules.weight(false, true)).isEqualTo(0.5);
        assertThat(PointRules.weight(false, false)).isZero();
    }

    @Test
    void the_flawless_bonus_is_rounded_up_last() {
        assertThat(PointRules.flawlessBonus(39)).isEqualTo(4);
        assertThat(PointRules.flawlessBonus(40)).isEqualTo(4);
        assertThat(PointRules.flawlessBonus(0)).isZero();
    }

    @Nested
    class Ranks {

        @Test
        void the_first_points_are_curious_v_level_1() {
            Rank rank = Rank.of(Ladder.CULTURE, 0);

            assertThat(rank.tierId()).isEqualTo("curious");
            assertThat(rank.division()).isEqualTo(5);
            assertThat(rank.level()).isEqualTo(1);
        }

        @Test
        void a_tier_starts_at_its_division_v_and_its_first_level() {
            Rank rank = Rank.of(Ladder.CULTURE, 1200);

            assertThat(rank.tierId()).isEqualTo("apprentice");
            assertThat(rank.division()).isEqualTo(5);
            assertThat(rank.level()).isEqualTo(71);
            assertThat(rank.nextAt()).isEqualTo(1200 + (4900 - 1200) / 5.0);
        }

        @Test
        void sage_asks_for_three_specialties_at_specialist() {
            Rank alone = Rank.of(Ladder.CULTURE, 300_000, new Specialties(1, 1, false));
            Rank three = Rank.of(Ladder.CULTURE, 300_000, new Specialties(3, 3, false));

            assertThat(alone.tierId()).isEqualTo("master");
            assertThat(alone.division()).isEqualTo(1);
            assertThat(alone.specialtiesMissing()).isEqualTo(2);
            assertThat(three.tierId()).isEqualTo("sage");
            assertThat(three.specialtiesMissing()).isZero();
        }

        @Test
        void grand_sage_asks_for_critical_thinking_besides_three_specialties() {
            Rank withinThree = Rank.of(Ladder.CULTURE, 500_000, new Specialties(3, 2, true));
            Rank without = Rank.of(Ladder.CULTURE, 500_000, new Specialties(4, 4, false));
            Rank both = Rank.of(Ladder.CULTURE, 500_000, new Specialties(4, 3, true));

            assertThat(withinThree.tierId()).isEqualTo("sage");
            assertThat(withinThree.specialtiesMissing()).isEqualTo(1);
            assertThat(without.tierId()).isEqualTo("sage");
            assertThat(without.criticalThinkingMissing()).isTrue();
            assertThat(both.tierId()).isEqualTo("grand-sage");
        }

        @Test
        void grand_sage_has_no_division_and_no_end() {
            Rank rank = Rank.of(Ladder.CULTURE, 2_000_000, new Specialties(4, 3, true));

            assertThat(rank.tierId()).isEqualTo("grand-sage");
            assertThat(rank.division()).isZero();
            assertThat(rank.level()).isGreaterThan(490 + 14);
        }

        @Test
        void the_steps_go_from_division_v_of_the_first_tier_to_the_top() {
            assertThat(Ladder.CULTURE.steps()).hasSize(7 * 5 + 1);
            assertThat(Ladder.CULTURE.steps().getFirst()).isEqualTo(new Ladder.Step(0, 5, 0));
            assertThat(Ladder.CULTURE.steps().getLast()).isEqualTo(new Ladder.Step(7, 0, 400000));
        }

        @Test
        void a_specialty_has_its_own_titles() {
            assertThat(Rank.of(Ladder.SPECIALTY, 5000).tierId()).isEqualTo("proficient");
            assertThat(Rank.of(Ladder.SPECIALTY, 100_000).tierId()).isEqualTo("luminary");
        }
    }

    @Nested
    class Stats {

        private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
        private final List<AttemptRecord> history = new ArrayList<>();
        private final List<SessionFinishedRecord> finished = new ArrayList<>();
        private final Course reseaux = CourseLoader.load(Path.of("../examples/reseaux"), new DiagnosticCollector()).orElseThrow();
        private final ContentRepository content = () -> List.of(reseaux);

        private StudyStats statsOn(String day) {
            Clock clock = Clock.fixed(Instant.parse(day + "T20:00:00Z"), PARIS);
            return new ReadStudyStats(content, new History(history, finished), RewardedSubjects.allBut("guide"), clock).read();
        }

        private void answer(String subject, String exercise, String at, double points) {
            history.add(new AttemptRecord(subject, exercise, Optional.of("s1"), Instant.parse(at), points > 0 ? Verdict.CORRECT : Verdict.WRONG, points));
        }

        @Test
        void counts_the_points_of_the_history_and_the_bonuses() {
            answer("reseaux", "s11-a1", "2026-10-20T08:00:00Z", 0);
            answer("reseaux", "s11-a2", "2026-10-21T08:05:00Z", 3);
            finished.add(new SessionFinishedRecord("reseaux", "s11", Instant.parse("2026-10-21T09:00:00Z"), 1));

            assertThat(statsOn("2026-10-21").points()).isEqualTo(4);
        }

        @Test
        void the_points_of_a_track_go_to_its_field() {
            answer("reseaux", "s11-a2", "2026-10-21T08:05:00Z", 30);
            answer("oc", "s34-a1", "2026-10-21T08:06:00Z", 5);

            StudyStats stats = statsOn("2026-10-21");

            assertThat(stats.fields()).singleElement().satisfies(field -> {
                assertThat(field.domain()).isEqualTo(Domain.INFORMATION_TECHNOLOGY);
                assertThat(field.points()).isEqualTo(30);
                assertThat(field.rank().tierId()).isEqualTo("novice");
            });
        }

        @Test
        void keeps_the_records_best_streak_flawless_sessions_and_first_tries() {
            answer("oc", "a", "2026-10-10T08:00:00Z", 1);
            answer("oc", "a", "2026-10-11T08:00:00Z", 0);
            answer("oc", "b", "2026-10-12T08:00:00Z", 0);
            answer("oc", "c", "2026-10-20T08:00:00Z", 1);
            finished.add(new SessionFinishedRecord("oc", "s1", Instant.parse("2026-10-20T09:00:00Z"), 2));
            finished.add(new SessionFinishedRecord("oc", "s1", Instant.parse("2026-10-21T09:00:00Z"), 0));

            assertThat(statsOn("2026-10-21").records()).isEqualTo(new Records(3, 1, 1, 2, 3));
        }

        @Test
        void a_subject_left_out_of_the_rewards_earns_nothing_and_keeps_no_streak() {
            answer("guide", "g1-q1", "2026-10-21T08:00:00Z", 1);
            finished.add(new SessionFinishedRecord("guide", "g1", Instant.parse("2026-10-21T09:00:00Z"), 1));

            assertThat(statsOn("2026-10-21").points()).isZero();
            assertThat(statsOn("2026-10-21").streakDays()).isZero();
        }

        @Test
        void a_streak_counts_the_days_in_a_row_and_survives_until_the_end_of_the_next_day() {
            answer("oc", "a", "2026-10-18T08:00:00Z", 1);
            answer("oc", "b", "2026-10-19T08:00:00Z", 1);
            answer("oc", "c", "2026-10-20T08:00:00Z", 1);

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
        @Override public void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, double points, Instant at) { }
        @Override public void saveSessionFinished(SessionKey key, Instant at, Duration spent, double bonus) { }
        @Override public Set<String> finishedSessions(String subject) { return Set.of(); }
        @Override public Map<String, Verdict> firstVerdicts(SessionKey session) { return Map.of(); }
        @Override public List<AttemptRecord> attemptHistory() { return attempts; }
        @Override public List<SessionFinishedRecord> sessionsFinished() { return finished; }
        @Override public Optional<LastActivity> lastActivity(String subject) { return Optional.empty(); }
    }
}
