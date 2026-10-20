package io.github.lucasssoh.boussole.core.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.ValueResponse;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The use cases of studying, on the example subject. */
class LearningTest {

    private static final ExerciseKey MAC_BITS = new ExerciseKey("reseaux", "s11-a1");

    private final ContentRepository content = new ExampleContentRepository();
    private final InMemoryProgressRepository progress = new InMemoryProgressRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void an_attempt_is_checked_and_recorded() {
        AttemptOutcome first = new SubmitAttempt(content, progress, clock).submit(MAC_BITS, new ValueResponse("6"), Optional.empty());
        AttemptOutcome second = new SubmitAttempt(content, progress, clock).submit(MAC_BITS, new ValueResponse("48"), Optional.empty());

        assertThat(first.result().verdict()).isEqualTo(Verdict.WRONG);
        assertThat(first.xpGained()).isEqualTo(5);
        assertThat(second.xpGained()).isEqualTo(5);
        assertThat(second.progress().attempts()).isEqualTo(2);
        assertThat(second.progress().solved()).isTrue();
    }

    @Test
    void the_solution_waits_for_a_first_attempt() {
        RevealSolution solution = new RevealSolution(content, progress);

        assertThatThrownBy(() -> solution.reveal(MAC_BITS)).isInstanceOf(LearningRuleException.class);

        new SubmitAttempt(content, progress, clock).submit(MAC_BITS, new ValueResponse("6"), Optional.empty());
        assertThat(solution.reveal(MAC_BITS).solution()).isPresent();
    }

    @Test
    void a_free_exercise_shows_its_solution_before_the_learner_grades_themself() {
        assertThat(new RevealSolution(content, progress).reveal(new ExerciseKey("reseaux", "s11-p1")).solution()).isPresent();
    }

    @Test
    void hints_come_one_at_a_time_until_none_is_left() {
        RevealNextHint hints = new RevealNextHint(content, progress, clock);

        RevealedHint first = hints.reveal(MAC_BITS);

        assertThat(first.number()).isEqualTo(1);
        assertThat(first.total()).isEqualTo(1);
        assertThatThrownBy(() -> hints.reveal(MAC_BITS)).isInstanceOf(LearningRuleException.class);
    }

    @Test
    void a_session_brings_the_exercises_it_asks_again() {
        new SubmitAttempt(content, progress, clock).submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.empty());

        OpenedSession review = new OpenSession(content, progress).open("reseaux", "r19");

        assertThat(review.referenced()).containsOnlyKeys("s11-a1", "s11-a2");
        assertThat(review.progress().get("s11-a2").solved()).isTrue();
    }

    @Test
    void a_finished_session_shows_in_the_course() {
        new FinishSession(content, progress, clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(new OpenCourse(content, progress).open("reseaux").finishedSessions()).containsExactly("s11");
    }

    @Test
    void the_course_resumes_in_the_session_the_learner_answered_in_last() {
        assertThat(new OpenCourse(content, progress).open("reseaux").resume()).contains("s11");

        new SubmitAttempt(content, progress, clock)
                .submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.of("r19"));
        CourseOverview overview = new OpenCourse(content, progress).open("reseaux");

        assertThat(overview.resume()).contains("r19");
        assertThat(overview.lastStudied()).contains(clock.instant());
    }

    @Test
    void a_finished_session_is_not_resumed() {
        new SubmitAttempt(content, progress, clock).submit(new ExerciseKey("reseaux", "r19-q1"), new ChoicesResponse(Set.of(1)), Optional.of("r19"));
        new FinishSession(content, progress, clock).finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5));

        assertThat(new OpenCourse(content, progress).open("reseaux").resume()).contains("s11");
    }

    @Test
    void an_unknown_exercise_is_not_found() {
        assertThatThrownBy(() -> new SubmitAttempt(content, progress, clock)
                .submit(new ExerciseKey("reseaux", "nope"), new ValueResponse("1"), Optional.empty()))
                .isInstanceOf(ContentNotFoundException.class);
    }
}
