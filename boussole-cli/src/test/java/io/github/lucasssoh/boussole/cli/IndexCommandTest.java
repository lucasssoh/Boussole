package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class IndexCommandTest {

    private final StringWriter out = new StringWriter();

    private int run(String... args) {
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(new StringWriter()));
        return command.execute(args);
    }

    @Test
    void everyQuestionComesWithHowOftenItIsAskedAgain() {
        assertThat(run("index", "../examples/reseaux")).isZero();
        assertThat(out.toString())
                .contains("6 questions in 2 sessions")
                .containsPattern("s11 +notion couche3 +s11-a1 +numeric +1  Combien de bits")
                .containsPattern("s11 +checkpoint +s11-c1 +mcq\\+ +0  ");
    }

    @Test
    void beforeKeepsOnlyTheEarlierSessions() {
        assertThat(run("index", "../examples/reseaux", "--before", "r19")).isZero();
        assertThat(out.toString()).contains("5 questions in 1 sessions").doesNotContain("r19-q1");
    }
}
