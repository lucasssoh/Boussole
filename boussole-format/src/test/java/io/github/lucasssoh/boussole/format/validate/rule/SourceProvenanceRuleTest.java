package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourceProvenanceRuleTest {

    @TempDir
    Path dir;

    @Test
    void a_document_from_the_web_says_its_licence() {
        var report = SubjectFixture.in(dir)
                .document("web/python-tutorial.md", "Notes")
                .document("web/pep8.md", "Notes")
                .sources("""
                        - file: web/python-tutorial.md
                          url: https://docs.python.org/3/tutorial/
                          title: The Python Tutorial
                          licence: PSF License
                          retrieved: 2026-10-21
                        - file: web/pep8.md
                          url: https://peps.python.org/pep-0008/
                        """)
                .session("s1", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F039)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).startsWith("`web/pep8.md` comes from https://peps.python.org/pep-0008/: add its `licence`"));
        assertThat(report.withCode(DiagnosticCode.F001)).isEmpty();
    }

    @Test
    void an_address_and_a_date_are_written_whole() {
        var report = SubjectFixture.in(dir)
                .document("web/a.md", "Notes")
                .sources("""
                        - file: web/a.md
                          url: docs.python.org
                        - file: web/a.md
                          url: https://docs.python.org/3/
                          licence: PSF License
                          retrieved: 21/10/2026
                        """)
                .session("s1", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F001)).extracting(diagnostic -> diagnostic.message()).containsExactly(
                "`url` must be a whole web address, such as `https://docs.python.org/3/`, not `docs.python.org`",
                "`retrieved` is a date written 2026-10-21, not `21/10/2026`");
    }
}
