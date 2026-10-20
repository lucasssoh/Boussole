package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClosingRuleTest {

    @TempDir
    Path dir;

    private static final String CHECKPOINT = ":::::checkpoint\n" + mcq("#c1", "- [x] Oui\n- [ ] Non") + ":::::\n";
    private static final String PRACTICE = ":::::practice\n" + exercise("p1") + ":::::\n";

    @Test
    void aLearnerWhoWantsAClosingExerciseGetsOne() {
        var report = SubjectFixture.in(dir).learner("closing: exercise\n")
                .session("s1", notion("a", "e1") + CHECKPOINT)
                .session("s2", notion("b", "e2") + CHECKPOINT + PRACTICE)
                .inspect();

        assertThat(report.withCode(DiagnosticCode.P006)).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.position().file()).endsWith("s1.md");
            assertThat(diagnostic.message()).contains("close each session with an exercise");
        });
    }

    @Test
    void bothMeansAQuizAndAnExercise() {
        var report = SubjectFixture.in(dir).learner("closing: both\n").session("s1", notion("a", "e1") + PRACTICE).inspect();

        assertThat(report.withCode(DiagnosticCode.P006)).extracting(Diagnostic::message).singleElement().asString()
                .contains("with a quiz");
    }

    @Test
    void withoutAProfileTheClosingIsFree() {
        assertThat(SubjectFixture.in(dir).session("s1", notion("a", "e1")).inspect().withCode(DiagnosticCode.P006)).isEmpty();
    }
}
