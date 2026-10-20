package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.qcm;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QcmChoicesRuleTest {

    @TempDir
    Path dir;

    @Test
    void oneChoiceIsTicked() {
        var report = inspect(qcm("#q1", "- [ ] Oui\n- [ ] Non"));

        assertThat(report.withCode(Code.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("no ticked choice"));
    }

    @Test
    void severalTickedChoicesNeedMultiple() {
        assertThat(inspect(qcm("#q1", "- [x] Oui\n- [x] Non")).withCode(Code.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("multiple=true"));
        assertThat(inspect(qcm("#q1 multiple=true", "- [x] Oui\n- [x] Non")).withCode(Code.F022)).isEmpty();
    }

    @Test
    void aQcmHasAtLeastTwoChoices() {
        assertThat(inspect(qcm("#q1", "- [x] Oui")).withCode(Code.F022)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("at least two"));
    }

    private io.github.lucasssoh.boussole.format.InspectionReport inspect(String qcm) {
        return SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + qcm).inspect();
    }
}
