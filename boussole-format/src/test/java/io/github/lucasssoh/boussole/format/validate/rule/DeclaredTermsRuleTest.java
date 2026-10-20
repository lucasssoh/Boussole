package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.qcm;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeclaredTermsRuleTest {

    @TempDir
    Path dir;

    private static final String LEXICON = "- id: mac\n  term: MAC\n  definition: Une adresse.\n";

    @Test
    void aTermUsedIsListedInTheFrontMatter() {
        var report = SubjectFixture.in(dir).lexicon(LEXICON)
                .session("s1", "La :term[MAC] et encore la :term[MAC].\n\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(Code.P005)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("`mac`"));
    }

    @Test
    void aListedTermIsFine() {
        var report = SubjectFixture.in(dir).lexicon(LEXICON)
                .session("s1", "terms: [mac]\n", "La :term[MAC].\n\n" + notion("a", "e1"))
                .inspect();

        assertThat(report.diagnostics()).isEmpty();
    }
}
