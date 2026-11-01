package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class BusCommandTest {

    @TempDir
    Path track;

    private final StringWriter err = new StringWriter();

    private int run(String... args) {
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setErr(new PrintWriter(err));
        return command.execute(args);
    }

    @Test
    void anAgentSaysWhatItWritesOnTheBusOfTheTrack() throws Exception {
        Files.writeString(track.resolve("subject.yaml"), "format: 1\nid: t\nname: T\nlang: fr\n");

        int status = run("bus", "writing", track.toString(), "--tool", "codex", "--agent", "ch1", "--file", "cursus/01-a/11-b.md");

        assertThat(status).isZero();
        String line = Files.readString(track.resolve(".boussole/bus/events.jsonl"));
        assertThat(line).matches("\\d+ \\{.*\\}\\n").contains("\"event\":\"writing\"", "\"tool\":\"codex\"", "\"session\":\"codex\"",
                "\"agent\":\"ch1\"", "\"file\":\"cursus/01-a/11-b.md\"");
    }

    @Test
    void anUnknownEventOrAFolderThatIsNoTrackIsRefused() {
        assertThat(run("bus", "flying", track.toString())).isNotZero();
        assertThat(err.toString()).contains("write one of start, doing, writing");
        assertThat(run("bus", "start", track.toString())).isNotZero();
        assertThat(err.toString()).contains("is not a track");
    }
}
