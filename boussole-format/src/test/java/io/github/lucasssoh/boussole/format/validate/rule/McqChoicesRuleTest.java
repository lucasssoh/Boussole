package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class McqChoicesRuleTest {

    @TempDir
    Path dir;

    @Test
    void oneChoiceIsTicked() {
        var report = inspect(mcq("#q1", "- [ ] Oui\n- [ ] Non"));

        assertThat(report.withCode(DiagnosticCode.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("no ticked choice"));
    }

    @Test
    void severalTickedChoicesNeedMultiple() {
        assertThat(inspect(mcq("#q1", "- [x] Oui\n- [x] Non")).withCode(DiagnosticCode.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("multiple=true"));
        assertThat(inspect(mcq("#q1 multiple=true", "- [x] Oui\n- [x] Non")).withCode(DiagnosticCode.F022)).isEmpty();
    }

    @Test
    void aMcqHasAtLeastTwoChoices() {
        assertThat(inspect(mcq("#q1", "- [x] Oui")).withCode(DiagnosticCode.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("at least two"));
    }

    private io.github.lucasssoh.boussole.format.InspectionReport inspect(String mcq) {
        return SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + mcq).inspect();
    }
}
