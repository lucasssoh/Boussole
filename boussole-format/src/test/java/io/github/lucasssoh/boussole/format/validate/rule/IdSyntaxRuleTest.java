package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.qcm;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IdSyntaxRuleTest {

    @TempDir
    Path dir;

    @Test
    void anIdHasNoCapitalOrUnderscore() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "S1_a1")).inspect();

        assertThat(report.withCode(DiagnosticCode.F004)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`S1_a1`"));
    }

    @Test
    void lowercaseLettersDigitsAndDashesAreFine() {
        var report = SubjectFixture.in(dir).session("s1", notion("le-masque-2", "s1-a1")).inspect();

        assertThat(report.withCode(DiagnosticCode.F004)).isEmpty();
    }
}
