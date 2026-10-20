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

class FillGapsRuleTest {

    @TempDir
    Path dir;

    @Test
    void theStatementAndTheAnswerHaveTheSameGaps() {
        var report = inspect("Le masque /24 s'écrit [[1]].", "1: 255.255.255.0\n2: oups");

        assertThat(report.withCode(Code.F023)).singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.message()).contains("[1]", "[1, 2]"));
    }

    @Test
    void matchingGapsAreFine() {
        var report = inspect("[[1]] et [[2]].", "1: a | b\n2: c");

        assertThat(report.withCode(Code.F023)).isEmpty();
        assertThat(report.errors()).isEmpty();
    }

    private io.github.lucasssoh.boussole.format.InspectionReport inspect(String statement, String answer) {
        return SubjectFixture.in(dir).session("s1", """
                ## A {#a}

                ::::exercise{#e1 type=fill}
                %s

                :::answer
                %s
                :::

                :::hint
                Indice.
                :::

                :::solution
                Solution.
                :::
                ::::
                """.formatted(statement, answer)).inspect();
    }
}
