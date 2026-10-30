package io.github.lucasssoh.boussole.core.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.lucasssoh.boussole.core.learn.check.ChoicesResponse;
import io.github.lucasssoh.boussole.core.learn.check.ValueResponse;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.FirstAnswer;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.progress.SessionProgress;
import io.github.lucasssoh.boussole.core.learn.reward.Achievement;
import io.github.lucasssoh.boussole.core.learn.reward.AchievementProgress;
import io.github.lucasssoh.boussole.core.learn.reward.QuestionScore;
import io.github.lucasssoh.boussole.core.learn.reward.ReadAchievements;
import io.github.lucasssoh.boussole.core.learn.reward.TrackScore;
import io.github.lucasssoh.boussole.core.learn.reward.RewardedSubjects;
import io.github.lucasssoh.boussole.format.model.SessionId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        assertThat(first.pointsGained()).isZero();
        assertThat(second.pointsGained()).isZero();
        assertThat(second.progress().attempts()).isEqualTo(2);
        assertThat(second.progress().solved()).isTrue();
    }

    @Test
    void a_right_first_answer_earns_its_points_times_the_level_and_half_of_them_in_another_session() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);

        AttemptOutcome lesson = submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("s11"), Optional.empty());
        AttemptOutcome review = submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("r19"), Optional.empty());
        AttemptOutcome reviewAgain = submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("r19"), Optional.empty());

        assertThat(lesson.score()).isEqualTo(new QuestionScore(1, 1));
        assertThat(lesson.pointsGained()).isEqualTo(3);
        assertThat(review.pointsGained()).isEqualTo(1.5);
        assertThat(reviewAgain.pointsGained()).isZero();
    }

    @Test
    void a_track_says_what_is_earned_missed_and_left_to_play() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("s11"), Optional.empty());
        submit.submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(0)), Optional.of("s11"), Optional.empty());

        TrackScore score = new OpenCourse(content, progress).open("reseaux", Optional.empty()).score();

        assertThat(score).isEqualTo(new TrackScore(3, 3, 18));
        assertThat(score.toPlay()).isEqualTo(12);
    }

    @Test
    void finishing_every_session_of_a_track_brings_its_achievements() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        FinishSession finish = new FinishSession(content, progress, RewardedSubjects.all(), clock);
        submit.submit(MAC_BITS, new ValueResponse("6"), Optional.of("s11"), Optional.empty());
        finish.finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(50));
        submit.submit(new ExerciseKey("reseaux", "r19-q1"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("r19"), Optional.empty());
        submit.submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        finish.finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5));

        Map<Achievement, AchievementProgress> achievements = new ReadAchievements(content, progress, RewardedSubjects.all(), clock).read().stream()
                .collect(Collectors.toMap(AchievementProgress::achievement, achievement -> achievement));

        assertThat(achievements).hasSize(Achievement.values().length);
        assertThat(achievements.get(Achievement.THE_FIRST_STEP).level()).isEqualTo(1);
        assertThat(achievements.get(Achievement.ITHACA).value()).isEqualTo(1);
        assertThat(achievements.get(Achievement.ON_THE_SHOULDERS_OF_GIANTS).level()).isEqualTo(1);
        assertThat(achievements.get(Achievement.THE_MADELEINE).value()).isEqualTo(1);
        assertThat(achievements.get(Achievement.TWENTY_TIMES_ON_THE_LOOM).value()).isEqualTo(1);
        assertThat(achievements.get(Achievement.THE_HONEST_MAN)).isEqualTo(new AchievementProgress(Achievement.THE_HONEST_MAN, 1, 0));
        assertThat(achievements.get(Achievement.DROP_BY_DROP).value()).isEqualTo(1);
    }

    @Test
    void a_session_without_a_mistake_earns_a_tenth_more_once() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        submit.submit(new ExerciseKey("reseaux", "r19-q1"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("r19"), Optional.empty());
        submit.submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        FinishSession finish = new FinishSession(content, progress, RewardedSubjects.all(), clock);

        assertThat(finish.finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5))).isEqualTo(1);
        assertThat(finish.finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5))).isZero();
    }

    @Test
    void a_subject_left_out_of_the_rewards_is_still_followed_but_earns_nothing() {
        RewardedSubjects notReseaux = RewardedSubjects.allBut("reseaux");

        AttemptOutcome outcome = new SubmitAttempt(content, progress, notReseaux, clock).submit(MAC_BITS, new ValueResponse("48"), Optional.empty(), Optional.empty());
        double finished = new FinishSession(content, progress, notReseaux, clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(outcome.pointsGained()).isZero();
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
        assertThat(new OpenSession(content, progress).open("reseaux", "s11", Optional.empty()).answered()).containsEntry("s11-a1", new FirstAnswer(Verdict.WRONG, 0));
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

        assertThat(overview.sessionProgress().get("s11")).isEqualTo(new SessionProgress(5, 2, 1, 0, 12));
        assertThat(overview.sessionProgress().get("r19")).isEqualTo(new SessionProgress(3, 2, 1, 0, 3));
    }

    @Test
    void the_stars_of_a_session_count_the_first_answers_to_its_own_questions() {
        SubmitAttempt submit = new SubmitAttempt(content, progress, RewardedSubjects.all(), clock);
        AttemptOutcome right = submit.submit(MAC_BITS, new ValueResponse("48"), Optional.of("s11"), Optional.empty());
        submit.submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(0)), Optional.of("s11"), Optional.empty());
        submit.submit(new ExerciseKey("reseaux", "s11-a2"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());

        CourseOverview overview = new OpenCourse(content, progress).open("reseaux", Optional.empty());

        assertThat(right.stars()).isEqualTo(3);
        assertThat(overview.sessionProgress().get("s11").stars()).isEqualTo(3);
        // A question asked again in a review is not one of its own: it shows its stars, but adds none.
        assertThat(overview.sessionProgress().get("r19").stars()).isZero();
        assertThat(new OpenSession(content, progress).open("reseaux", "s11", Optional.empty()).answered())
                .containsEntry("s11-a1", new FirstAnswer(Verdict.CORRECT, 3));
    }

    @Test
    void a_finished_session_is_not_resumed() {
        new SubmitAttempt(content, progress, RewardedSubjects.all(), clock).submit(new ExerciseKey("reseaux", "r19-q1"), new ChoicesResponse(Set.of(1)), Optional.of("r19"), Optional.empty());
        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("reseaux", "r19"), Duration.ofMinutes(5));

        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).resume()).contains("s11");
    }

    @Test
    void a_session_waits_for_its_prerequisites_until_they_are_finished() {
        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).waitingFor())
                .containsOnlyKeys("r19")
                .containsEntry("r19", List.of(new SessionId("reseaux", "couche-reseau", "s11")));

        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).waitingFor()).isEmpty();
    }

    @Test
    void the_course_resumes_in_a_session_whose_prerequisites_are_finished() {
        new FinishSession(content, progress, RewardedSubjects.all(), clock).finish(new SessionKey("reseaux", "s11"), Duration.ofMinutes(52));

        assertThat(new OpenCourse(content, progress).open("reseaux", Optional.empty()).resume()).contains("r19");
    }

    @Test
    void an_unknown_exercise_is_not_found() {
        assertThatThrownBy(() -> new SubmitAttempt(content, progress, RewardedSubjects.all(), clock)
                .submit(new ExerciseKey("reseaux", "nope"), new ValueResponse("1"), Optional.empty(), Optional.empty()))
                .isInstanceOf(ContentNotFoundException.class);
    }
}
