package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.qcm;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TimedSessionContentRuleTest {

    @TempDir
    Path dir;

    @Test
    void aTimedSessionOnlyAsksQuestions() {
        var report = SubjectFixture.in(dir).timedSession("e1", "exam", 5, notion("a", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.P009)).extracting(Diagnostic::message).containsExactly(
                "an exam asks questions: notion `a` belongs in a lesson",
                "an exam shows no worked example; its solutions come after the answers");
    }

    @Test
    void lessonRulesDoNotApplyToTimedSessions() {
        String noHint = exercise("e1").replace(":::hint\nIndice.\n:::\n", "").replace("type=text", "type=text minutes=5");
        var report = SubjectFixture.in(dir).learner("closing: both\nquick_check: true\n")
                .timedSession("e1", "exam", 5, noHint)
                .inspect();

        assertThat(report.diagnostics()).isEmpty();
    }
}
