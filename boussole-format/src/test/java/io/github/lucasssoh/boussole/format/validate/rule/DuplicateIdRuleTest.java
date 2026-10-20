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

class DuplicateIdRuleTest {

    @TempDir
    Path dir;

    @Test
    void anExerciseIdIsUniqueInTheWholeSubject() {
        var report = SubjectFixture.in(dir)
                .session("s1", notion("a", "e1"))
                .session("s2", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F003)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("exercise ID `e1`", "s1.md"));
    }

    @Test
    void aNotionIdIsUniqueInItsSession() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "e1") + notion("a", "e2")).inspect();

        assertThat(report.withCode(DiagnosticCode.F003)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("notion ID `a`"));
    }

    @Test
    void theSameNotionIdInTwoSessionsIsFine() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "e1")).session("s2", notion("a", "e2")).inspect();

        assertThat(report.withCode(DiagnosticCode.F003)).isEmpty();
    }
}
