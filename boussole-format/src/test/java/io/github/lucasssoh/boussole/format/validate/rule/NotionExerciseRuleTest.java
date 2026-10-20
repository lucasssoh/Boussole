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

class NotionExerciseRuleTest {

    @TempDir
    Path dir;

    @Test
    void aNotionEndsWithSomethingToDo() {
        var report = SubjectFixture.in(dir).session("s1", "## Rien à faire {#vide}\n\nExplication.\n\n" + notion("b", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.P001)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`vide`"));
    }

    @Test
    void aMcqIsSomethingToDo() {
        var report = SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + mcq("#q1", "- [x] Oui\n- [ ] Non")).inspect();

        assertThat(report.withCode(DiagnosticCode.P001)).isEmpty();
    }
}
