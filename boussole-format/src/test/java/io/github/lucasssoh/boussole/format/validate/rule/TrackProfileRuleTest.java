package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import io.github.lucasssoh.boussole.format.model.Domain;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** P013, F043: how demanding a track is, its field, and where its hook questions sit. */
class TrackProfileRuleTest {

    @TempDir
    Path dir;

    @Test
    void aTrackSaysHowDemandingItIsAndItsField() {
        var report = SubjectFixture.in(dir).session("s1", notion("a", "e1")).inspect();

        assertThat(report.diagnostics()).isEmpty();
        assertThat(report.course().orElseThrow().subject().level()).contains(TrackLevel.BACHELOR);
        assertThat(report.course().orElseThrow().subject().domain()).contains(Domain.INFORMATION_TECHNOLOGY);
    }

    @Test
    void withoutThemTheValidatorAsks() {
        var report = SubjectFixture.in(dir).subjectYaml("format: 1\nid: t\nname: T\nlang: fr\n").session("s1", notion("a", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.P013)).extracting(diagnostic -> diagnostic.message()).containsExactly(
                "`subject.yaml` has no `level`: write what the track takes to follow it, discovery, secondary, bachelor, master or research",
                "`subject.yaml` has no `domain`: write its field of knowledge from the list of docs/FORMAT.md §2, such as information-technology");
    }

    @Test
    void anUnknownLevelOrFieldIsAnError() {
        var report = SubjectFixture.in(dir)
                .subjectYaml("format: 1\nid: t\nname: T\nlang: fr\nlevel: phd\ndomain: computing\n")
                .session("s1", notion("a", "e1"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F001)).extracting(diagnostic -> diagnostic.message()).containsExactly(
                "`level` is discovery, secondary, bachelor, master or research, not `phd`",
                "`domain` `computing` is not in the list of docs/FORMAT.md §2 (such as information-technology, mathematics, philosophy-and-ethics)");
    }

    @Test
    void aHookBelongsInTheWarmup() {
        var report = SubjectFixture.in(dir)
                .session("s1", notion("a", "e1"))
                .session("s2", """
                        :::::warmup
                        ::::mcq{#w1 hook=true}
                        À ton avis ?

                        - [x] Oui
                        - [ ] Non
                          > Non.

                        :::hint
                        Devine.
                        :::

                        :::solution
                        Oui.
                        :::
                        ::::
                        :::::

                        """ + notion("b", "e2").replace("{#e2", "{#e2 hook=true"))
                .inspect();

        assertThat(report.withCode(DiagnosticCode.F043)).extracting(diagnostic -> diagnostic.message()).containsExactly(
                "`e2` is a hook (`hook=true`), which belongs in the warmup: move it there, or remove `hook=true` so that it earns points");
    }
}
