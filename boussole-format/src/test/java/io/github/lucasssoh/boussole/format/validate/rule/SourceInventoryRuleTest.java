package io.github.lucasssoh.boussole.format.validate.rule;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourceInventoryRuleTest {

    /** SHA-256 of "cours". */
    private static final String COURS = "37f49b5db412cdfa7354a598ab21b7d8c7950c834b490500cfc27b4b80d519fe";

    @TempDir
    Path dir;

    @Test
    void withoutARecordTheDocumentsAreReportedOnce() {
        var report = SubjectFixture.in(dir).document("cours.md", "cours").document("td.md", "td").inspect();

        assertThat(report.withCode(DiagnosticCode.F033)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("2 documents", "cursus/sources.yaml"));
    }

    @Test
    void aNewDocumentIsReported() {
        var report = SubjectFixture.in(dir)
                .document("cours.md", "cours")
                .document("td2.md", "td")
                .sources("- file: cours.md\n  status: used\n")
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F033)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("new document `td2.md`"));
    }

    @Test
    void aChangedDocumentIsReported() {
        var report = SubjectFixture.in(dir)
                .document("cours.md", "cours, corrigé")
                .sources("- file: cours.md\n  sha256: " + COURS + "\n")
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F034)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`cours.md` changed"));
    }

    @Test
    void aPendingDocumentStillAsksForWork() {
        var report = SubjectFixture.in(dir)
                .document("chapitre4.md", "à venir")
                .sources("- file: chapitre4.md\n  status: pending\n")
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F033)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("waits"));
    }

    @Test
    void aPatternCoversSeveralDocuments() {
        var report = SubjectFixture.in(dir)
                .document("annales/2024.md", "a")
                .document("annales/2025.md", "b")
                .sources("- file: \"annales/**\"\n  status: ignored\n")
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F033)).isEmpty();
    }

    @Test
    void aDocumentGoneIsReported() {
        var report = SubjectFixture.in(dir)
                .document("cours.md", "cours")
                .sources("- file: cours.md\n- file: ancien.pdf\n")
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F035)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`ancien.pdf`"));
    }

    @Test
    void anUnknownStatusIsAnError() {
        var report = SubjectFixture.in(dir).sources("- file: cours.md\n  status: done\n").inspect();

        assertThat(report.withCode(DiagnosticCode.F001)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("used, ignored, pending"));
    }
}
