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

class SessionIdMatchRuleTest {

    @TempDir
    Path dir;

    @Test
    void theFileIdIsTheSyllabusId() {
        var report = SubjectFixture.in(dir)
                .sessionFile("s1", "---\nid: s2\nobjectives: [Faire.]\n---\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F010)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`id: s2`", "`s1`"));
    }
}
