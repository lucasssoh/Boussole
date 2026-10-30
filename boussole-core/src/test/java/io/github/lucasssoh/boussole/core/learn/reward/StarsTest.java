package io.github.lucasssoh.boussole.core.learn.reward;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import org.junit.jupiter.api.Test;

class StarsTest {

    @Test
    void the_stars_are_the_share_of_the_score_earned() {
        assertThat(Stars.of(new QuestionScore(1, 1), Verdict.CORRECT)).isEqualTo(3);
        assertThat(Stars.of(new QuestionScore(3, 4), Verdict.PARTLY)).isEqualTo(2);
        assertThat(Stars.of(new QuestionScore(0.5, 1), Verdict.PARTLY)).isEqualTo(2);
        assertThat(Stars.of(new QuestionScore(1, 4), Verdict.PARTLY)).isEqualTo(1);
        assertThat(Stars.of(new QuestionScore(0, 1), Verdict.WRONG)).isZero();
    }

    @Test
    void a_multiple_choice_split_in_thirds_reaches_three_stars_when_all_is_right() {
        assertThat(Stars.of(new QuestionScore(3 * (1.0 / 3), 1), Verdict.CORRECT)).isEqualTo(3);
        assertThat(Stars.of(new QuestionScore(1.0 / 3, 1), Verdict.PARTLY)).isEqualTo(1);
    }

    @Test
    void a_question_that_earns_nothing_takes_the_stars_of_its_verdict() {
        assertThat(Stars.of(QuestionScore.NOTHING, Verdict.CORRECT)).isEqualTo(3);
        assertThat(Stars.of(QuestionScore.NOTHING, Verdict.PARTLY)).isEqualTo(2);
        assertThat(Stars.of(QuestionScore.NOTHING, Verdict.WRONG)).isZero();
    }
}
