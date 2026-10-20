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

class SourceFileRuleTest {

    @TempDir
    Path dir;

    @Test
    void aSourceIsInTheSourcesFolder() {
        var report = SubjectFixture.in(dir)
                .session("s1", "sources: [absent.pdf]\n", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F031)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`absent.pdf` is not in sources/"));
    }

    @Test
    void aPageIsInTheDocument() {
        var report = SubjectFixture.in(dir)
                .pdf("cours.pdf", 3)
                .session("s1", "sources: [cours.pdf#page=3]\n",
                        "::pdf[Cours]{src=cours.pdf page=4}\n\nVoir :pdf[le cours]{src=cours.pdf page=2}.\n\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F031)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("has 3 pages, not 4"));
    }

    @Test
    void aSourceCannotLeaveTheSourcesFolder() {
        var report = SubjectFixture.in(dir).session("s1", "::pdf[x]{src=../subject.yaml}\n\n" + notion("a", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.F031)).hasSize(1);
    }
}
