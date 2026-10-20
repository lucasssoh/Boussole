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

class QuickCheckRuleTest {

    @TempDir
    Path dir;

    private static final String QUICK = qcm("#q1", "- [x] Oui\n- [ ] Non");

    @Test
    void eachNotionAsksAQcmBeforeItsExample() {
        String checked = "## A {#a}\n\nExplication.\n\n" + QUICK + "\n:::example\nExemple.\n:::\n\n" + exercise("e1");
        var report = SubjectFixture.in(dir).learner("quick_check: true\n")
                .session("s1", checked + "\n" + notion("b", "e2"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.P007)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("notion `b`"));
    }

    @Test
    void withoutQuickCheckNothingIsAsked() {
        var report = SubjectFixture.in(dir).learner("quick_check: false\n").session("s1", notion("a", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.P007)).isEmpty();
    }
}
