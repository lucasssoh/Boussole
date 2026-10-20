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

class VideoExistsRuleTest {

    @TempDir
    Path dir;

    private static final String BODY = "## A {#a}\n\n::video[Vue]{youtube=aircAruvnKk}\n::video[Fausse]{youtube=xxxxxxxxxxx}\n\n"
            + exercise("e1");

    @Test
    void eachVideoIsKnownToTheCatalog() {
        var report = SubjectFixture.in(dir).videos("aircAruvnKk"::equals).session("s1", BODY).inspect();

        assertThat(report.withCode(DiagnosticCode.F032)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("xxxxxxxxxxx"));
    }

    @Test
    void withoutCatalogVideosAreNotChecked() {
        assertThat(SubjectFixture.in(dir).session("s1", BODY).inspect().withCode(DiagnosticCode.F032)).isEmpty();
    }
}
