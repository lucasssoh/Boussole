package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.notion;
import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PartOrderRuleTest {

    @TempDir
    Path dir;

    @Test
    void theWarmupComesFirstAndThePracticeLast() {
        var report = SubjectFixture.in(dir).session("s1", """
                :::::practice
                %s
                :::::

                %s

                :::::warmup
                %s
                :::::
                """.formatted(exercise("p1"), notion("a", "e1"), exercise("w1"))).inspect();

        assertThat(report.withCode(DiagnosticCode.P003)).extracting(diagnostic -> diagnostic.message())
                .containsExactly("the practice comes last in a session", "the warmup comes first in a session");
    }

    @Test
    void anIntroductionMayNotPrecedeTheWarmup() {
        var report = SubjectFixture.in(dir).session("s1", "Introduction.\n\n:::::warmup\n" + exercise("w1") + ":::::\n"
                + notion("a", "e1")).inspect();

        assertThat(report.withCode(DiagnosticCode.P003)).hasSize(1);
    }
}
