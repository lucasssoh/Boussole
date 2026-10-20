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

class MissingSessionFileRuleTest {

    @TempDir
    Path dir;

    @Test
    void anApprovedSyllabusExpectsEverySessionFile() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "e1")).plannedSession("s2").inspect();

        assertThat(report.withCode(DiagnosticCode.F011)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("cursus/01-ch/s2.md"));
        assertThat(report.hasErrors()).isFalse();
    }

    @Test
    void aProposedSyllabusHasNoFileYet() {
        var report = SubjectFixture.in(dir).status("proposed").plannedSession("s1").inspect();

        assertThat(report.diagnostics()).isEmpty();
    }
}
