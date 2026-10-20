package io.github.lucasssoh.boussole.format.parse;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Code;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostics;
import io.github.lucasssoh.boussole.format.load.SessionReader;
import io.github.lucasssoh.boussole.format.model.Session;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.answer.NumericAnswer;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.answer.Rational;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.Prose;
import io.github.lucasssoh.boussole.format.model.block.Video;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The problems a session file shows on its own, found while it is read. */
class SessionParsingTest {

    private final Diagnostics diagnostics = new Diagnostics();

    private Session parse(String body) {
        return SessionReader.read("---\nid: s1\nobjectives: [Faire.]\n---\n" + body, "s1.md", diagnostics);
    }

    private List<String> messages(Code code) {
        return diagnostics.list().stream().filter(d -> d.code() == code).map(Diagnostic::message).toList();
    }

    private Exercise onlyExercise(Session session) {
        return session.blocks().filter(Exercise.class::isInstance).map(Exercise.class::cast).findFirst().orElseThrow();
    }

    private static String exerciseOfType(String attributes, String answer) {
        return "::::exercise{#e1 " + attributes + "}\nQuestion ?\n\n:::answer\n" + answer
                + "\n:::\n\n:::solution\nSolution.\n:::\n::::\n";
    }

    @Nested
    class Structure {

        @Test
        void containersNestByTheirColons() {
            Session session = parse(":::::warmup\n" + exercise("w1") + ":::::\n\n## A {#a}\n\nTexte.\n");

            assertThat(diagnostics.list()).isEmpty();
            assertThat(session.parts()).hasSize(2);
            assertThat(onlyExercise(session).hints()).hasSize(1);
        }

        @Test
        void linesAreCountedFromTheTopOfTheFile() {
            parse("Texte.\n\n:::exemple\n:::\n");

            assertThat(diagnostics.list()).singleElement()
                    .satisfies(diagnostic -> assertThat(diagnostic.position()).hasToString("s1.md:7"));
        }

        @Test
        void nothingInFencedCodeIsADirective() {
            Session session = parse("## A {#a}\n\n```\n:::example\n# pas un titre\n```\n");

            assertThat(diagnostics.list()).isEmpty();
            assertThat(session.parts().getFirst().blocks()).singleElement().isInstanceOf(Prose.class);
        }

        @Test
        void aContainerIsClosed() {
            parse("## A {#a}\n\n:::example\nTexte.\n");

            assertThat(messages(Code.F026)).containsExactly("`example` is never closed");
        }

        @Test
        void aClosingLineClosesAnOpenContainer() {
            parse("## A {#a}\n\n::::\n");

            assertThat(messages(Code.F026)).singleElement().asString().contains("no container is open");
        }

        @Test
        void aSessionHasNoLevelOneHeading() {
            parse("# Titre\n");

            assertThat(messages(Code.F012)).hasSize(1);
        }

        @Test
        void aNotionHasAnId() {
            Session session = parse("## Sans ID\n\nTexte.\n");

            assertThat(messages(Code.F013)).singleElement().asString().contains("{#an-id}");
            assertThat(session.parts().getFirst()).isInstanceOf(Notion.class);
        }

        @Test
        void textBeforeTheFirstNotionIsAnIntroduction() {
            Session session = parse("Introduction.\n\n## A {#a}\n\nTexte.\n");

            assertThat(session.parts()).extracting(part -> part.getClass().getSimpleName())
                    .containsExactly("TextPart", "Notion");
        }

        @Test
        void aSessionStartsWithItsFrontMatter() {
            SessionReader.read("## A {#a}\n", "s1.md", diagnostics);

            assertThat(messages(Code.F001)).singleElement().asString().contains("front matter");
        }
    }

    @Nested
    class Directives {

        @Test
        void anUnknownDirectiveIsReported() {
            parse(":::exemple\nTexte.\n:::\n");

            assertThat(messages(Code.F020)).containsExactly("unknown directive `exemple`");
        }

        @Test
        void aDirectiveHasItsKind() {
            parse(":::video{youtube=aircAruvnKk}\n:::\n::example\n");

            assertThat(messages(Code.F020)).containsExactly(
                    "`video` is a one-line directive: ::video",
                    "`example` is a container: :::example … :::");
        }

        @Test
        void aDirectiveIsInItsPlace() {
            parse(":::example\n::ref{id=e1}\n:::\n\n:::hint\nIndice.\n:::\n\n:::example\n:::::warmup\n:::::\n:::\n");

            assertThat(messages(Code.F020)).containsExactly(
                    "`ref` is not allowed in `example`",
                    "`hint` is only allowed inside `exercise` or `qcm`, as :::hint",
                    "`warmup` is only allowed at the top level of a session");
        }

        @Test
        void attributesAreKnownAndRequiredOnesGiven() {
            parse("::video[Vue]{youtube=aircAruvnKk speed=2}\n::pdf[Cours]\n:::example{x}\n:::\n");

            assertThat(messages(Code.F027)).containsExactly(
                    "`speed` is not an attribute of `video`",
                    "`pdf` needs the attribute `src`",
                    "`x` is not an attribute of `example`");
        }

        @Test
        void attributeValuesAreChecked() {
            parse("::video{youtube=court}\n::video{youtube=aircAruvnKk start=1:00 end=0:30}\n::pdf{src=a.pdf page=zero}\n");

            assertThat(messages(Code.F027)).containsExactly(
                    "`video`: `youtube` must be the 11-character ID of a video, not `court`",
                    "`video`: `end` must come after `start`",
                    "`pdf`: `page` must be a whole number, not `zero`");
        }

        @Test
        void aVideoSegmentIsRead() {
            Session session = parse("::video[Vue]{youtube=aircAruvnKk start=2:10 end=1:02:03}\n");

            Block video = session.blocks().findFirst().orElseThrow();
            assertThat(video).isInstanceOfSatisfying(Video.class, found -> {
                assertThat(found.start()).contains(Duration.ofSeconds(130));
                assertThat(found.end()).contains(Duration.ofSeconds(3723));
            });
        }
    }

    @Nested
    class Exercises {

        @Test
        void anExerciseHasExactlyOneAnswerAndOneSolution() {
            parse("::::exercise{#e1 type=text}\nQuestion ?\n::::\n");

            assertThat(messages(Code.F021)).containsExactly(
                    "`exercise` needs exactly one `answer`, it has 0",
                    "`exercise` needs exactly one `solution`, it has 0");
        }

        @Test
        void aFreeExerciseHasNoAnswer() {
            parse(exerciseOfType("type=free", "oui"));

            assertThat(messages(Code.F021)).singleElement().asString().contains("free exercise has no `answer`");
        }

        @Test
        void aQcmHasNoAnswer() {
            parse("::::qcm{#q1}\nQ ?\n\n- [x] Oui\n- [ ] Non\n\n:::answer\nOui\n:::\n\n:::solution\nOui.\n:::\n::::\n");

            assertThat(messages(Code.F021)).singleElement().asString().contains("tick the right choices");
        }

        @Test
        void theTypeIsKnown() {
            parse(exerciseOfType("type=essay", "x"));

            assertThat(messages(Code.F027)).singleElement().asString().contains("must be one of numeric, text, fill, order, match, free");
        }

        @Test
        void anAttributeBelongsToTheType() {
            parse(exerciseOfType("type=text tolerance=1", "x"));

            assertThat(messages(Code.F027)).singleElement().asString().contains("`tolerance` is not an attribute of a text exercise");
        }

        @Test
        void aNumericAnswerIsANumber() {
            parse(exerciseOfType("type=numeric", "quarante-huit"));

            assertThat(messages(Code.F024)).hasSize(1);
        }

        @Test
        void numericAnswersAcceptFractionsAndCommas() {
            Exercise exercise = onlyExercise(parse(exerciseOfType("type=numeric tolerance=0,01", "3/4\n0,75")));

            assertThat(diagnostics.list()).isEmpty();
            assertThat(exercise.answer()).get().isInstanceOfSatisfying(NumericAnswer.class, answer -> {
                assertThat(answer.values()).containsExactly(rational("3/4"), rational("3/4"));
                assertThat(answer.tolerance()).isEqualTo(rational("1/100"));
            });
        }

        @Test
        void aRegularExpressionCompiles() {
            parse(exerciseOfType("type=text", "re:(forwarding"));

            assertThat(messages(Code.F025)).hasSize(1);
        }

        @Test
        void fillOrderAndMatchAnswersAreRead() {
            Exercise fill = onlyExercise(parse(exerciseOfType("type=fill", "1: a | b\n2: re:c+")));
            assertThat(fill.answer()).get().isInstanceOfSatisfying(FillAnswer.class,
                    answer -> assertThat(answer.gaps()).extracting(gap -> gap.accepted().size()).containsExactly(2, 1));

            Exercise order = onlyExercise(parse(exerciseOfType("type=order", "1. Physique\n2. Liaison\n3. Réseau")));
            assertThat(order.answer()).get().isInstanceOfSatisfying(OrderAnswer.class,
                    answer -> assertThat(answer.items()).containsExactly("Physique", "Liaison", "Réseau"));

            Exercise match = onlyExercise(parse(exerciseOfType("type=match", "TCP => transport\nIP => réseau")));
            assertThat(match.answer()).get().isInstanceOfSatisfying(MatchAnswer.class,
                    answer -> assertThat(answer.pairs()).containsExactly(new MatchPair("TCP", "transport"), new MatchPair("IP", "réseau")));

            assertThat(diagnostics.list()).isEmpty();
        }

        @Test
        void malformedAnswerLinesAreReported() {
            parse(exerciseOfType("type=match", "TCP transport\nIP => réseau"));
            parse(exerciseOfType("type=order", "Physique\nLiaison"));
            parse(exerciseOfType("type=fill", "a | b"));

            assertThat(messages(Code.F021)).containsExactly(
                    "a match answer line reads `left => right`",
                    "an order answer is a list: `1. first item`",
                    "a fill answer line reads `1: answer | other answer`");
        }

        @Test
        void textAfterTheChoicesOfAQcmIsReported() {
            parse("::::qcm{#q1}\nQ ?\n\n- [x] Oui\n- [ ] Non\n\nEt ceci ?\n\n:::solution\nOui.\n:::\n::::\n");

            assertThat(messages(Code.F022)).hasSize(1);
        }

        private Rational rational(String text) {
            return Rational.parse(text).orElseThrow();
        }
    }
}
