package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChapterOrderRuleTest {

    @TempDir
    Path dir;

    /** Two chapters, a and b, of two sessions each, in the folder of the fixture. */
    private static final String SYLLABUS = """
            status: approved
            chapters:
              - id: a
                title: A
                dir: 01-ch
                sessions:
                  - {id: s1, title: Un, duration: 60, goal: Faire., file: s1.md}
                  - {id: s2, title: Deux, duration: 60, goal: Faire., file: s2.md}
              - id: b
                title: B
                dir: 01-ch
                sessions:
                  - {id: s3, title: Trois, duration: 60, goal: Faire., file: s3.md}
                  - {id: s4, title: Quatre, duration: 60, goal: Faire., file: s4.md}
            """;

    @Test
    void chaptersThatFollowOneOrderPass() {
        var report = SubjectFixture.in(dir)
                .syllabusYaml(SYLLABUS)
                .session("s1", notion("a", "e1"))
                .session("s2", "prerequisites: [test.a.s1]\n", notion("b", "e2"))
                .session("s3", "prerequisites: [test.a.s2]\n", notion("c", "e3"))
                .session("s4", "prerequisites: [test.b.s3]\n", notion("d", "e4"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F044)).isEmpty();
    }

    @Test
    void chaptersThatWaitForOneAnotherAreReported() {
        var report = SubjectFixture.in(dir)
                .syllabusYaml(SYLLABUS)
                .session("s1", notion("a", "e1"))
                .session("s2", "prerequisites: [test.b.s3]\n", notion("b", "e2"))
                .session("s3", "prerequisites: [test.a.s1]\n", notion("c", "e3"))
                .session("s4", notion("d", "e4"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F044)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).isEqualTo("chapters wait for one another: `a` waits for `b`"
                        + " waits for `a`; a chapter is finished before the chapters that need it, so move a session or a"
                        + " prerequisite until the chapters follow one order"));
    }
}
