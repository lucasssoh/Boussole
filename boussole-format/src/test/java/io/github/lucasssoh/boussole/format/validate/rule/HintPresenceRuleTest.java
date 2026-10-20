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

class HintPresenceRuleTest {

    @TempDir
    Path dir;

    @Test
    void anExerciseHasAHint() {
        String withoutHint = exercise("e1").replace(":::hint\nIndice.\n:::\n", "");
        var report = SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + withoutHint).inspect();

        assertThat(report.withCode(Code.P004)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`e1`"));
        assertThat(report.errors()).isEmpty();
    }
}
