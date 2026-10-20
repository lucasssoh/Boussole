package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.qcm;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TimedSessionFitRuleTest {

    @TempDir
    Path dir;

    /** {@code count} single-choice qcms, one minute each. */
    private static String qcms(String prefix, int count) {
        StringBuilder body = new StringBuilder();
        for (int index = 1; index <= count; index++) {
            body.append(qcm("#" + prefix + index, "- [x] Oui\n- [ ] Non")).append('\n');
        }
        return body.toString();
    }

    @Test
    void theQuestionsFillTheTimeAllowed() {
        var report = SubjectFixture.in(dir)
                .timedSession("e1", "exam", 20, qcms("q", 18))
                .timedSession("e2", "exam", 20, qcms("r", 5))
                .timedSession("e3", "review", 10, qcms("s", 15))
                .inspect();

        assertThat(report.withCode(Code.P008)).extracting(Diagnostic::message).containsExactly(
                "the questions take about 5 min at a normal pace, the session allows 20: add questions",
                "the questions take about 15 min at a normal pace, the session allows 10: remove questions");
    }

    @Test
    void buildingAnAnswerTakesLongerThanChoosingOne() {
        String built = exercise("b1").replace("type=text", "type=numeric").replace("oui", "4")
                + exercise("b2").replace("type=text", "type=numeric").replace("oui", "4");
        var report = SubjectFixture.in(dir).timedSession("e1", "exam", 10, built + qcms("q", 4)).inspect();

        assertThat(report.withCode(Code.P008)).isEmpty();
    }

    @Test
    void aFillTakesAMinutePerTypedGapAndHalfAMinutePerChoice() {
        String fill = exercise("f1").replace("type=text", "type=fill").replace("Question ?", "[[1]] [[2]] [[3]] [[4]]")
                .replace("oui", "1: x\n2: number 4\n3: choice *a | b\n4: choice *c | d");
        var report = SubjectFixture.in(dir).timedSession("e1", "review", 5, fill + qcms("q", 2)).inspect();

        assertThat(report.errors()).isEmpty();
        assertThat(report.withCode(Code.P008)).isEmpty();
    }

    @Test
    void minutesOverrideTheEstimateAndRefsCount() {
        var report = SubjectFixture.in(dir)
                .session("s1", notion("a", "a1"))
                .timedSession("e1", "review", 12, qcm("#q1 minutes=9", "- [x] Oui\n- [ ] Non") + "\n::ref{id=a1}\n")
                .inspect();

        assertThat(report.errors()).isEmpty();
        assertThat(report.withCode(Code.P008)).isEmpty();
    }

    @Test
    void lessonsAreNotTimed() {
        assertThat(SubjectFixture.in(dir).session("s1", notion("a", "e1")).inspect().withCode(Code.P008)).isEmpty();
    }
}
