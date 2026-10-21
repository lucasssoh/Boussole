package io.github.lucasssoh.boussole.format.parse;

import static io.github.lucasssoh.boussole.format.fixture.Snippets.exercise;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.SessionFileReader;
import io.github.lucasssoh.boussole.format.model.SessionContent;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;
import io.github.lucasssoh.boussole.format.model.answer.MatchPair;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.expectation.ChoiceExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import io.github.lucasssoh.boussole.format.model.answer.OrderAnswer;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ProseBlock;
import io.github.lucasssoh.boussole.format.model.block.FileVideoSource;
import io.github.lucasssoh.boussole.format.model.block.LinkBlock;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.block.LinkInlineDirective;
import io.github.lucasssoh.boussole.format.model.block.VideoBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphBlock;
import io.github.lucasssoh.boussole.format.model.block.GraphEdge;
import io.github.lucasssoh.boussole.format.model.block.GraphLayout;
import io.github.lucasssoh.boussole.format.model.block.GraphNode;
import io.github.lucasssoh.boussole.format.model.block.GraphPoint;
import java.util.Optional;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The problems a session file shows on its own, found while it is read. */
class SessionParsingTest {

    private final DiagnosticCollector diagnostics = new DiagnosticCollector();

    private SessionContent parse(String body) {
        return SessionFileReader.read("---\nid: s1\nobjectives: [Faire.]\n---\n" + body, "s1.md", diagnostics);
    }

    private List<String> messages(DiagnosticCode code) {
        return diagnostics.list().stream().filter(d -> d.code() == code).map(Diagnostic::message).toList();
    }

    private ExerciseBlock onlyExercise(SessionContent session) {
        return session.blocks().filter(ExerciseBlock.class::isInstance).map(ExerciseBlock.class::cast).findFirst().orElseThrow();
    }

    private static String exerciseOfType(String attributes, String answer) {
        return "::::exercise{#e1 " + attributes + "}\nQuestion ?\n\n:::answer\n" + answer
                + "\n:::\n\n:::solution\nSolution.\n:::\n::::\n";
    }

    @Nested
    class Structure {

        @Test
        void containersNestByTheirColons() {
            SessionContent session = parse(":::::warmup\n" + exercise("w1") + ":::::\n\n## A {#a}\n\nTexte.\n");

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
            SessionContent session = parse("## A {#a}\n\n```\n:::example\n# pas un titre\n```\n");

            assertThat(diagnostics.list()).isEmpty();
            assertThat(session.parts().getFirst().blocks()).singleElement().isInstanceOf(ProseBlock.class);
        }

        @Test
        void aContainerIsClosed() {
            parse("## A {#a}\n\n:::example\nTexte.\n");

            assertThat(messages(DiagnosticCode.F026)).containsExactly("`example` is never closed");
        }

        @Test
        void aClosingLineClosesAnOpenContainer() {
            parse("## A {#a}\n\n::::\n");

            assertThat(messages(DiagnosticCode.F026)).singleElement().asString().contains("no container is open");
        }

        @Test
        void aSessionHasNoLevelOneHeading() {
            parse("# Titre\n");

            assertThat(messages(DiagnosticCode.F012)).hasSize(1);
        }

        @Test
        void aNotionHasAnId() {
            SessionContent session = parse("## Sans ID\n\nTexte.\n");

            assertThat(messages(DiagnosticCode.F013)).singleElement().asString().contains("{#an-id}");
            assertThat(session.parts().getFirst()).isInstanceOf(NotionPart.class);
        }

        @Test
        void textBeforeTheFirstNotionIsAnIntroduction() {
            SessionContent session = parse("Introduction.\n\n## A {#a}\n\nTexte.\n");

            assertThat(session.parts()).extracting(part -> part.getClass().getSimpleName())
                    .containsExactly("TextPart", "NotionPart");
        }

        @Test
        void aSessionStartsWithItsFrontMatter() {
            SessionFileReader.read("## A {#a}\n", "s1.md", diagnostics);

            assertThat(messages(DiagnosticCode.F001)).singleElement().asString().contains("front matter");
        }
    }

    @Nested
    class Directives {

        @Test
        void anUnknownDirectiveIsReported() {
            parse(":::exemple\nTexte.\n:::\n");

            assertThat(messages(DiagnosticCode.F020)).containsExactly("unknown directive `exemple`");
        }

        @Test
        void aDirectiveHasItsKind() {
            parse(":::video{youtube=aircAruvnKk}\n:::\n::example\n");

            assertThat(messages(DiagnosticCode.F020)).containsExactly(
                    "`video` is a one-line directive: ::video",
                    "`example` is a container: :::example … :::");
        }

        @Test
        void aDirectiveIsInItsPlace() {
            parse(":::example\n::ref{id=e1}\n:::\n\n:::hint\nIndice.\n:::\n\n:::example\n:::::warmup\n:::::\n:::\n");

            assertThat(messages(DiagnosticCode.F020)).containsExactly(
                    "`ref` is not allowed in `example`",
                    "`hint` is only allowed inside `exercise` or `mcq`, as :::hint",
                    "`warmup` is only allowed at the top level of a session");
        }

        @Test
        void attributesAreKnownAndRequiredOnesGiven() {
            parse("::video[Vue]{youtube=aircAruvnKk speed=2}\n::pdf[Cours]\n:::example{x}\n:::\n");

            assertThat(messages(DiagnosticCode.F027)).containsExactly(
                    "`speed` is not an attribute of `video`",
                    "`pdf` needs the attribute `src`",
                    "`x` is not an attribute of `example`");
        }

        @Test
        void attributeValuesAreChecked() {
            parse("::video{youtube=court}\n::video{youtube=aircAruvnKk start=1:00 end=0:30}\n::pdf{src=a.pdf page=zero}\n");

            assertThat(messages(DiagnosticCode.F027)).containsExactly(
                    "`video`: `youtube` must be the 11-character ID of a video, not `court`",
                    "`video`: `end` must come after `start`",
                    "`pdf`: `page` must be a whole number, not `zero`");
        }

        @Test
        void aWebPageIsCitedAsABlockOrInASentence() {
            SessionContent session = parse("::link[La documentation de Python]{url=https://docs.python.org/3/}\n\n"
                    + "Voir :link[la page de la PEP 8]{url=https://peps.python.org/pep-0008/}.\n\n"
                    + "::link[Mauvais]{url=docs.python.org}\n\nEt :link[ici]{url=ftp://exemple.org}.\n");

            assertThat(session.blocks().findFirst().orElseThrow()).isInstanceOfSatisfying(LinkBlock.class,
                    link -> assertThat(link.url()).isEqualTo("https://docs.python.org/3/"));
            assertThat(session.blocks().filter(ProseBlock.class::isInstance).map(ProseBlock.class::cast)
                    .flatMap(prose -> prose.inlines().stream()).filter(LinkInlineDirective.class::isInstance).toList()).hasSize(1);
            assertThat(messages(DiagnosticCode.F027)).containsExactly(
                    "`link`: `url` must be a whole web address, such as `https://docs.python.org/3/`, not `docs.python.org`",
                    "`link`: `url` must be a whole web address, such as `https://docs.python.org/3/`, not `ftp://exemple.org`");
        }

        @Test
        void aVideoComesFromYoutubeOrFromAFileNeverBoth() {
            SessionContent session = parse("::video[Cours 3]{src=enregistrements/cours3.mp4 start=12:30}\n"
                    + "::video[Les deux]{youtube=aircAruvnKk src=a.mp4}\n::video[Aucune]\n::video[Vieux format]{src=cours1.avi}\n");

            assertThat(session.blocks().findFirst().orElseThrow()).isInstanceOfSatisfying(VideoBlock.class, video -> {
                assertThat(video.source()).isInstanceOfSatisfying(FileVideoSource.class,
                        local -> assertThat(local.file().file()).isEqualTo("enregistrements/cours3.mp4"));
                assertThat(video.start()).contains(Duration.ofSeconds(750));
            });
            assertThat(messages(DiagnosticCode.F027)).containsExactly(
                    "`video`: `youtube` or `src` is needed, one of them: `youtube=<video ID>` for YouTube, `src=<file>` for a video of the sources folder",
                    "`video`: `youtube` or `src` is needed, one of them: `youtube=<video ID>` for YouTube, `src=<file>` for a video of the sources folder");
            assertThat(messages(DiagnosticCode.F038)).singleElement().asString().startsWith("`cours1.avi` may not play in a browser");
        }

        @Test
        void aVideoSegmentIsRead() {
            SessionContent session = parse("::video[Vue]{youtube=aircAruvnKk start=2:10 end=1:02:03}\n");

            Block video = session.blocks().findFirst().orElseThrow();
            assertThat(video).isInstanceOfSatisfying(VideoBlock.class, found -> {
                assertThat(found.start()).contains(Duration.ofSeconds(130));
                assertThat(found.end()).contains(Duration.ofSeconds(3723));
            });
        }
    }

    @Nested
    class Graphs {

        @Test
        void nodesAndEdgesAreReadInTheOrderTheyAppear() {
            SessionContent session = parse("""
                    :::graph[Plus court chemin]{layout=layers}
                    s "Source" !
                    s -> a : 4
                    s -> b : 2 !
                    a -- b
                    :::
                    """);

            assertThat(diagnostics.list()).isEmpty();
            assertThat(session.blocks().findFirst().orElseThrow()).isInstanceOfSatisfying(GraphBlock.class, graph -> {
                assertThat(graph.label()).isEqualTo("Plus court chemin");
                assertThat(graph.layout()).isEqualTo(GraphLayout.LAYERS);
                assertThat(graph.nodes()).containsExactly(
                        new GraphNode("s", Optional.of("Source"), Optional.empty(), true),
                        new GraphNode("a", Optional.empty(), Optional.empty(), false),
                        new GraphNode("b", Optional.empty(), Optional.empty(), false));
                assertThat(graph.edges()).containsExactly(
                        new GraphEdge("s", "a", true, Optional.of("4"), false),
                        new GraphEdge("s", "b", true, Optional.of("2"), true),
                        new GraphEdge("a", "b", false, Optional.empty(), false));
            });
        }

        @Test
        void placesGivenByTheAuthorAreKept() {
            SessionContent session = parse(":::graph\nu @ 0,0\nv @ 2,1\nu -- v\n:::\n");

            GraphBlock graph = (GraphBlock) session.blocks().findFirst().orElseThrow();
            assertThat(graph.placedByAuthor()).isTrue();
            assertThat(graph.nodes().get(1).at()).contains(new GraphPoint(2, 1));
        }

        @Test
        void eachMistakeSaysWhatToWrite() {
            parse("""
                    :::graph
                    a => b
                    a -> a
                    a @ 0,0
                    a
                    :::
                    :::graph
                    u @ 0,0
                    u -- v
                    w @ 0,0
                    :::
                    """);

            assertThat(messages(DiagnosticCode.F037)).containsExactly(
                    "`a => b` is neither a node (`a \"Label\" @ 0,1`) nor an edge (`a -> b : 3` or `a -- b`), with `!` at the end to make it stand out",
                    "`a` is linked to itself: a loop is not drawn, say it in the text",
                    "node `a` is given twice: write it on one line",
                    "either every node gives its place with `@ x,y`, or none does; missing: v",
                    "`u` and `w` are both at 0,0");
        }

        @Test
        void aLargeGraphIsAWarning() {
            StringBuilder body = new StringBuilder(":::graph{layout=row}\n");
            for (int index = 0; index < 13; index++) {
                body.append("n").append(index).append(" -> n").append(index + 1).append('\n');
            }
            parse(body.append(":::\n").toString());

            assertThat(messages(DiagnosticCode.P011)).singleElement().asString().startsWith("this graph has 14 nodes");
        }
    }

        @Nested
    class Exercises {

        @Test
        void anExerciseHasExactlyOneAnswerAndOneSolution() {
            parse("::::exercise{#e1 type=text}\nQuestion ?\n::::\n");

            assertThat(messages(DiagnosticCode.F021)).containsExactly(
                    "`exercise` needs exactly one `answer`, it has 0",
                    "`exercise` needs exactly one `solution`, it has 0");
        }

        @Test
        void aFreeExerciseHasNoAnswer() {
            parse(exerciseOfType("type=free", "oui"));

            assertThat(messages(DiagnosticCode.F021)).singleElement().asString().contains("free exercise has no `answer`");
        }

        @Test
        void aMcqHasNoAnswer() {
            parse("::::mcq{#q1}\nQ ?\n\n- [x] Oui\n- [ ] Non\n\n:::answer\nOui\n:::\n\n:::solution\nOui.\n:::\n::::\n");

            assertThat(messages(DiagnosticCode.F021)).singleElement().asString().contains("tick the right choices");
        }

        @Test
        void theTypeIsKnown() {
            parse(exerciseOfType("type=essay", "x"));

            assertThat(messages(DiagnosticCode.F027)).singleElement().asString().contains("must be one of numeric, text, fill, order, match, free");
        }

        @Test
        void anAttributeBelongsToTheType() {
            parse(exerciseOfType("type=text tolerance=1", "x"));

            assertThat(messages(DiagnosticCode.F027)).singleElement().asString().contains("`tolerance` is not an attribute of a text exercise");
        }

        @Test
        void aNumericAnswerIsANumber() {
            parse(exerciseOfType("type=numeric", "quarante-huit"));

            assertThat(messages(DiagnosticCode.F024)).hasSize(1);
        }

        @Test
        void numericAnswersAcceptFractionsAndCommas() {
            ExerciseBlock exercise = onlyExercise(parse(exerciseOfType("type=numeric tolerance=0,01", "3/4\n0,75")));

            assertThat(diagnostics.list()).isEmpty();
            assertThat(exercise.answer()).get().isInstanceOfSatisfying(SingleAnswer.class, answer ->
                    assertThat(answer.expected()).isInstanceOfSatisfying(NumberExpectation.class, number -> {
                        assertThat(number.values()).containsExactly(rational("3/4"), rational("3/4"));
                        assertThat(number.tolerance()).isEqualTo(rational("1/100"));
                    }));
        }

        @Test
        void aRegularExpressionCompiles() {
            parse(exerciseOfType("type=text", "re:(forwarding"));

            assertThat(messages(DiagnosticCode.F025)).hasSize(1);
        }

        @Test
        void fillOrderAndMatchAnswersAreRead() {
            ExerciseBlock fill = onlyExercise(parse(exerciseOfType("type=fill", "1: a | b\n2: re:c+")));
            assertThat(fill.answer()).get().isInstanceOfSatisfying(FillAnswer.class, answer ->
                    assertThat(answer.gaps()).extracting(gap -> ((TextExpectation) gap.expected()).accepted().size())
                            .containsExactly(2, 1));

            ExerciseBlock order = onlyExercise(parse(exerciseOfType("type=order", "1. Physique\n2. Liaison\n3. Réseau")));
            assertThat(order.answer()).get().isInstanceOfSatisfying(OrderAnswer.class,
                    answer -> assertThat(answer.items()).containsExactly("Physique", "Liaison", "Réseau"));

            ExerciseBlock match = onlyExercise(parse(exerciseOfType("type=match", "TCP => transport\nIP => réseau")));
            assertThat(match.answer()).get().isInstanceOfSatisfying(MatchAnswer.class,
                    answer -> assertThat(answer.pairs()).containsExactly(new MatchPair("TCP", "transport"), new MatchPair("IP", "réseau")));

            assertThat(diagnostics.list()).isEmpty();
        }

        @Test
        void aGapIsTypedTextANumberOrAChoice() {
            ExerciseBlock proof = onlyExercise(parse(exerciseOfType("type=fill",
                    "1: x | $x$\n2: number 3/4 ±0.01\n3: choice $\\leq$ | *$\\geq$ | $=$")));

            assertThat(diagnostics.list()).isEmpty();
            assertThat(proof.answer()).get().isInstanceOfSatisfying(FillAnswer.class, answer -> {
                assertThat(answer.gaps().get(0).expected()).isInstanceOf(TextExpectation.class);
                assertThat(answer.gaps().get(1).expected()).isInstanceOfSatisfying(NumberExpectation.class, number -> {
                    assertThat(number.values()).containsExactly(rational("0.75"));
                    assertThat(number.tolerance()).isEqualTo(rational("0.01"));
                });
                assertThat(answer.gaps().get(2).expected()).isInstanceOfSatisfying(ChoiceExpectation.class, choice -> {
                    assertThat(choice.options()).containsExactly("$\\leq$", "$\\geq$", "$=$");
                    assertThat(choice.correctOption()).isEqualTo("$\\geq$");
                });
            });
        }

        @Test
        void aChoiceGapStarsOneOptionAndANumberGapIsANumber() {
            parse(exerciseOfType("type=fill", "1: choice a | b"));
            parse(exerciseOfType("type=fill", "1: choice *a | *b"));
            parse(exerciseOfType("type=fill", "1: number beaucoup"));

            assertThat(messages(DiagnosticCode.F021)).hasSize(2).allMatch(message -> message.contains("one starred"));
            assertThat(messages(DiagnosticCode.F024)).hasSize(1);
        }

        @Test
        void malformedAnswerLinesAreReported() {
            parse(exerciseOfType("type=match", "TCP transport\nIP => réseau"));
            parse(exerciseOfType("type=order", "Physique\nLiaison"));
            parse(exerciseOfType("type=fill", "a | b"));

            assertThat(messages(DiagnosticCode.F021)).containsExactly(
                    "a match answer line reads `left => right`",
                    "an order answer is a list: `1. first item`",
                    "a fill answer line reads `1: answer | other answer`, `2: number 62` or `3: choice *right | wrong`");
        }

        @Test
        void textAfterTheChoicesOfAMcqIsReported() {
            parse("::::mcq{#q1}\nQ ?\n\n- [x] Oui\n- [ ] Non\n\nEt ceci ?\n\n:::solution\nOui.\n:::\n::::\n");

            assertThat(messages(DiagnosticCode.F022)).hasSize(1);
        }

        @Test
        void aTaskListInsideCodeBelongsToTheStatementOfAMcq() {
            SessionContent session = parse("""
                    ::::mcq{#q1}
                    Laquelle de ces écritures est un QCM valide ?

                    ```markdown
                    - [x] Oui
                    - [x] Non
                    ```

                    - [x] La première
                    - [ ] Aucune
                      > Relis la syntaxe des choix.

                    :::solution
                    La première.
                    :::
                    ::::
                    """);

            assertThat(messages(DiagnosticCode.F022)).isEmpty();
            McqBlock mcq = (McqBlock) session.blocks().findFirst().orElseThrow();
            assertThat(mcq.choices()).extracting(McqChoice::text).containsExactly("La première", "Aucune");
        }

        private Rational rational(String text) {
            return Rational.parse(text).orElseThrow();
        }
    }
}
