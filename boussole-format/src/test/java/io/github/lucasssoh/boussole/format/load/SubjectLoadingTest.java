package io.github.lucasssoh.boussole.format.load;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The problems of the YAML files, found while a subject is read. */
class SubjectLoadingTest {

    @TempDir
    Path dir;

    @Test
    void aSubjectHasItsSubjectFile() {
        InspectionReport report = new SubjectInspector().inspect(dir);

        assertThat(report.course()).isEmpty();
        assertThat(report.diagnostics()).singleElement()
                .hasToString("subject.yaml: F001 the file is missing");
    }

    @Test
    void anUnknownFormatStopsTheReading() {
        InspectionReport report = SubjectFixture.in(dir).subjectYaml("format: 2\nid: test\nname: T\nlang: fr\n").inspect();

        assertThat(report.course()).isEmpty();
        assertThat(report.withCode(Code.F002)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.position()).hasToString("subject.yaml:1"));
    }

    @Test
    void requiredFieldsArePresent() {
        InspectionReport report = SubjectFixture.in(dir).subjectYaml("format: 1\nid: test\n").inspect();

        assertThat(report.withCode(Code.F001)).extracting(Diagnostic::message)
                .containsExactly("`name` is required", "`lang` is required");
        assertThat(report.course()).isPresent();
    }

    @Test
    void invalidYamlIsReportedWhereItIs() {
        InspectionReport report = SubjectFixture.in(dir).subjectYaml("format: 1\nid: [test\nname: T\n").inspect();

        assertThat(report.withCode(Code.F001)).first()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).startsWith("not valid YAML"));
    }

    @Test
    void theSyllabusIsChecked() {
        InspectionReport report = SubjectFixture.in(dir).syllabusYaml("""
                status: finished
                chapters:
                  - id: ch
                    title: Chapitre
                    dir: 01-ch
                    sessions:
                      - id: s1
                        title: Séance
                        duration: 300
                        file: s1.md
                """).inspect();

        assertThat(report.withCode(Code.F001)).extracting(Diagnostic::message).containsExactly(
                "`status` is `proposed` or `approved`, not `finished`",
                "`goal` is required",
                "`duration` is between 15 and 120 minutes, not 300");
    }

    @Test
    void aSessionHasObjectives() {
        InspectionReport report = SubjectFixture.in(dir).sessionFile("s1", "---\nid: s1\n---\n" + notion("a", "e1")).inspect();

        assertThat(report.withCode(Code.F001)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`objectives` is required"));
    }

    @Test
    void aSourceNamesAPage() {
        InspectionReport report = SubjectFixture.in(dir).pdf("cours.pdf", 2)
                .session("s1", "sources: [cours.pdf#page=0]\n", notion("a", "e1")).inspect();

        assertThat(report.withCode(Code.F001)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.position()).hasToString("cursus/01-ch/s1.md:4"));
    }
}
