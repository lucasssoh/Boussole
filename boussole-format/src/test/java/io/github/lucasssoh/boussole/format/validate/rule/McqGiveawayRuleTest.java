package io.github.lucasssoh.boussole.format.validate.rule;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.mcq;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.fixture.SubjectFixture;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class McqGiveawayRuleTest {

    @TempDir
    Path dir;

    private List<String> messages(String choices) {
        return SubjectFixture.in(dir).session("s1", "## A {#a}\n\n" + mcq("#q1", choices)).inspect()
                .withCode(DiagnosticCode.P015).stream().map(diagnostic -> diagnostic.message()).toList();
    }

    @Test
    void theRightChoiceIsTheOnlyOneGivingAReason() {
        String choices = """
                - [ ] $x_1$ et $x_2$
                  > Non.
                - [ ] $x_4$ seulement
                  > Non.
                - [x] $x_3$ et $x_5$ : le sommet est à l'intersection des droites $x_3 = 0$ et $x_5 = 0$
                """;

        assertThat(messages(choices)).containsExactly("the right choice of `q1` is the only one to give a reason, which gives"
                + " the answer away: give each wrong choice the reason a learner would wrongly give, in the same form, or move"
                + " the reason into the solution");
    }

    @Test
    void theRightChoiceIsMuchLongerThanTheOthers() {
        String choices = """
                - [x] Pour limiter la portée des annonces et la taille de la base dans les grands réseaux.
                - [ ] Pour se passer de table.
                  > Non.
                - [ ] Pour aller entre deux AS.
                  > Non.
                """;

        assertThat(messages(choices)).singleElement().asString()
                .startsWith("the right choice of `q1` is much longer than the wrong ones (84 characters against at most 25)");
    }

    @Test
    void choicesOfTheSameFormPass() {
        String choices = """
                - [x] Oui, parce que la base est réalisable
                - [ ] Non, parce que $\\bar c^{\\,3} < 0$
                  > Non.
                - [ ] Non, car $x_1 = 1$ n'est pas nul
                  > Non.
                """;

        assertThat(messages(choices)).isEmpty();
    }

    @Test
    void aColonInCodeIsNotAReason() {
        String choices = """
                - [x] `a -> b : 3`
                - [ ] `a -> a`
                  > Non.
                - [ ] `a => b`
                  > Non.
                """;

        assertThat(messages(choices)).isEmpty();
    }

    @Test
    void anEnglishColonGivesAReasonToo() {
        String choices = """
                - [x] Yes: the source is given
                - [ ] No
                  > No.
                - [ ] Only once
                  > No.
                """;

        assertThat(messages(choices)).singleElement().asString().contains("the only one to give a reason");
    }

    @Test
    void aFormulaCountsByItsSymbolsNotItsLatex() {
        assertThat(McqGiveawayRule.visible("$\\bar c^{\\,3} \\le 0$ et `x`")).isEqualTo("c,30 et x");
    }
}
