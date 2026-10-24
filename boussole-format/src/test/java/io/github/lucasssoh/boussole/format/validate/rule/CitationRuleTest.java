package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import io.github.lucasssoh.boussole.format.model.block.CalloutBlock;
import io.github.lucasssoh.boussole.format.model.block.CalloutKind;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** P014 and {@code :::beyond}: what a notion says comes from somewhere the learner can open. */
class CitationRuleTest {

    @TempDir
    Path dir;

    private static String notion(String id, String text) {
        return "## Notion " + id + " {#" + id + "}\n\n" + text + "\n\n:::example[Exemple]\nUn cas.\n:::\n\n" + exercise(id + "-e");
    }

    @Test
    void aNotionOfATrackWithDocumentsCitesItsSource() {
        var report = SubjectFixture.in(dir)
                .document("cours.md", "cours")
                .session("s1", notion("cited", "Comme le dit le cours :link[ici]{url=https://example.org}.")
                        + notion("bare", "Une affirmation sans source.")
                        + notion("aside", "Le cours.\n\n:::beyond\nDe culture générale : une analogie.\n:::"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.P014)).extracting(diagnostic -> diagnostic.message()).containsExactlyInAnyOrder(
                "notion `bare` cites no source: point to the page it relies on with :pdf[…]{src=… page=…}, or :link[…]{url=…}"
                        + " for a web source, and put what comes from elsewhere in a :::beyond that says where",
                "notion `aside` cites no source: point to the page it relies on with :pdf[…]{src=… page=…}, or :link[…]{url=…}"
                        + " for a web source, and put what comes from elsewhere in a :::beyond that says where");
        assertThat(report.course().orElseThrow().blocks().filter(CalloutBlock.class::isInstance).map(CalloutBlock.class::cast))
                .extracting(CalloutBlock::kind).containsExactly(CalloutKind.BEYOND);
    }

    @Test
    void aTrackWithoutDocumentsIsNotAsked() {
        var report = SubjectFixture.in(dir).session("s1", notion("bare", "Une affirmation.")).inspect();

        assertThat(report.withCode(DiagnosticCode.P014)).isEmpty();
    }
}
