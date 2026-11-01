package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import io.github.lucasssoh.boussole.format.model.ChapterLinks;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlannedPrerequisitesRuleTest {

    @TempDir
    Path dir;

    private static final String SYLLABUS = """
            status: proposed
            chapters:
              - id: a
                title: A
                dir: 01-ch
                sessions:
                  - {id: s1, title: Un, duration: 60, goal: Faire., file: s1.md}
              - id: b
                title: B
                dir: 01-ch
                sessions:
                  - {id: s2, title: Deux, duration: 60, goal: Faire., file: s2.md, prerequisites: [test.a.s1]}
            """;

    @Test
    void thePlanOrdersTheChaptersBeforeAnySessionIsWritten() {
        var report = SubjectFixture.in(dir).syllabusYaml(SYLLABUS).inspect();

        assertThat(report.course()).hasValueSatisfying(course -> assertThat(ChapterLinks.of(course).after("b")).isEqualTo(Set.of("a")));
        assertThat(report.withCode(DiagnosticCode.F030)).isEmpty();
    }

    @Test
    void aSessionThatRepeatsThePlanPasses() {
        var report = SubjectFixture.in(dir).syllabusYaml(SYLLABUS)
                .session("s1", notion("a", "e1"))
                .session("s2", "prerequisites: [test.a.s1]\n", notion("b", "e2"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F045)).isEmpty();
    }

    @Test
    void aSessionThatForgetsThePlannedPrerequisitesIsReported() {
        var report = SubjectFixture.in(dir).syllabusYaml(SYLLABUS)
                .session("s1", notion("a", "e1"))
                .session("s2", notion("b", "e2"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F045)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).isEqualTo("`prerequisites` of `s2` are none but the syllabus"
                        + " plans `test.a.s1`: write `prerequisites: [test.a.s1]` in the front matter, or change both"));
    }

    @Test
    void aPlannedPrerequisiteThatIsNoSessionIsReported() {
        var report = SubjectFixture.in(dir).syllabusYaml(SYLLABUS.replace("test.a.s1", "test.a.s9")).inspect();

        assertThat(report.withCode(DiagnosticCode.F030)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("prerequisite `test.a.s9` of `s2` is not a session"));
    }
}
