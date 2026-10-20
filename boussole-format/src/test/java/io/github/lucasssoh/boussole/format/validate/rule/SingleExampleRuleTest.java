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

class SingleExampleRuleTest {

    @TempDir
    Path dir;

    @Test
    void aNotionHasOneExample() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "e1") + "\n:::example\nUn autre.\n:::\n").inspect();

        assertThat(report.withCode(Code.P002)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("2 examples"));
    }
}
