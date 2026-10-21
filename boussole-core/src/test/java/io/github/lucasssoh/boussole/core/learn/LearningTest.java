package io.github.lucasssoh.boussole.core.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.ValueResponse;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
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
        AttemptOutcome first = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(MAC_BITS, new ValueResponse("6"), Optional.empty(), Optional.empty());
        AttemptOutcome second = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(MAC_BITS, new ValueResponse("48"), Optional.empty(), Optional.empty());

        assertThat(first.result().verdict()).isEqualTo(Verdict.WRONG);
        assertThat(first.xpGained()).isEqualTo(5);
        assertThat(second.xpGained()).isEqualTo(5);
        assertThat(second.progress().attempts()).isEqualTo(2);
        assertThat(second.progress().solved()).isTrue();
    }

    @Test
    void a_subject_left_out_of_the_rewards_is_still_followed_but_earns_nothing() {
        RewardedSubjects notReseaux = RewardedSubjects.allBut("reseaux");

        AttemptOutcome outcome = new SubmitAttempt(content, progress, notReseaux, clock).submit(MAC_BITS, new ValueResponse("48"), Optional.empty(), Optional.empty());
        int finished = new FinishSession(content, progress, notReseaux, clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(outcome.xpGained()).isZero();
        assertThat(outcome.progress().solved()).isTrue();
        assertThat(finished).isZero();
        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).finishedSessions()).containsExactly("s11");
    }

    @Test
    void in_a_lesson_the_first_answer_is_final_and_shows_when_the_session_opens_again() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        submit.submit(MAC_BITS, new ValueResponse("6"), Optional.of("s11"), Optional.empty());

        assertThatThrownBy(() -> submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("s11"), Optional.empty()))
                .isInstanceOf(LearningRuleException.class);
        assertThat(new OpenSession(content, progress).open("reseaux", "s11", Optional.empty()).answered()).containsEntry("s11-a1", Verdict.WRONG);
    }

    @Test
    void a_review_can_be_sat_again() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        ExerciseKey reviewed = new ExerciseKey("reseaux", "r19-q1");
        submit.submit(reviewed, new ChoicesResponse(Set.of(0)), Optional.of("r19"), Optional.empty());

        assertThat(submit.submit(reviewed, new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty()).progress().attempts()).isEqualTo(2);
        assertThat(new OpenSession(content, progress).open("reseaux", "r19", Optional.empty()).answered()).isEmpty();
    }

    @Test
    void the_solution_waits_for_a_first_attempt() {
        RevealSolution solution = new RevealSolution(content, progress);

        assertThatThrownBy(() -> solution.reveal(MAC_BITS, Optional.empty())).isInstanceOf(LearningRuleException.class);

        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(MAC_BITS, new ValueResponse("6"), Optional.empty(), Optional.empty());
        assertThat(solution.reveal(MAC_BITS, Optional.empty()).solution()).isPresent();
    }

    @Test
    void a_free_exercise_shows_its_solution_before_the_learner_grades_themself() {
        assertThat(new RevealSolution(content, progress).reveal(new ExerciseKey("reseaux", "s11-p1"), Optional.empty()).solution()).isPresent();
    }

    @Test
    void hints_come_one_at_a_time_until_none_is_left() {
        RevealNextHint hints = new RevealNextHint(content, progress, clock);

        RevealedHint first = hints.reveal(MAC_BITS, Optional.empty());

        assertThat(first.number()).isEqualTo(1);
        assertThat(first.total()).isEqualTo(1);
        assertThatThrownBy(() -> hints.reveal(MAC_BITS, Optional.empty())).isInstanceOf(LearningRuleException.class);
    }

    @Test
    void a_session_brings_the_exercises_it_asks_again() {
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.empty(), Optional.empty());

        OpenedSession review = new OpenSession(content, progress).open("reseaux", "r19", Optional.empty());

        assertThat(review.referenced()).containsOnlyKeys("s11-a1", "s11-a2");
        assertThat(review.progress().get("s11-a2").solved()).isTrue();
    }

    @Test
    void a_finished_session_shows_in_the_course() {
        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).finishedSessions()).containsExactly("s11");
    }

    @Test
    void the_course_resumes_in_the_session_the_learner_answered_in_last() {
        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).resume()).contains("s11");

        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock)
                .submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        CourseOverview overview = new OpenCourse(content, progress).open("reseaux", Optional.empty());

        assertThat(overview.resume()).contains("r19");
        assertThat(overview.lastStudied()).contains(clock.instant());
    }

    @Test
    void the_course_says_how_far_the_learner_is_in_each_session() {
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(new ExerciseKey("reseaux", "s11-a1"), new ValueResponse("48"), Optional.empty(), Optional.empty());
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(0)), Optional.empty(), Optional.empty());

        CourseOverview overview = new OpenCourse(content, progress).open("reseaux", Optional.empty());

        assertThat(overview.sessionProgress().get("s11")).isEqualTo(new SessionProgress(5, 2, 1));
        assertThat(overview.sessionProgress().get("r19")).isEqualTo(new SessionProgress(3, 2, 1));
    }

    @Test
    void a_finished_session_is_not_resumed() {
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(new ExerciseKey("reseaux", "r19-q1"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5));

        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).resume()).contains("s11");
    }

    @Test
    void an_unknown_exercise_is_not_found() {
        assertThatThrownBy(() -> new SubmitAttempt(content, progress, RewardedSubjects.all(), clock)
                .submit(new ExerciseKey("reseaux", "nope"), new ValueResponse("1"), Optional.empty(), Optional.empty()))
                .isInstanceOf(ContentNotFoundException.class);
    }
}
