package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class McqFeedbackRuleTest {

    @TempDir
    Path dir;

    @Test
    void eachWrongChoiceSaysWhyItIsWrong() {
        String choices = "- [x] Oui\n- [ ] Non\n  > Non, parce que.\n- [ ] Peut-être";
        var report = SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + mcq("#q1", choices)).inspect();

        assertThat(report.withCode(DiagnosticCode.P010)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`q1`"));
    }

    @Test
    void theRightChoiceNeedsNoFeedback() {
        var report = SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + mcq("#q1", "- [x] Oui\n- [ ] Non\n  > Non.")).inspect();

        assertThat(report.withCode(DiagnosticCode.P010)).isEmpty();
    }
}
