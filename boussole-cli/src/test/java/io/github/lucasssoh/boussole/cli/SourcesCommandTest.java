package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class SourcesCommandTest {

    @TempDir
    Path dir;

    private final StringWriter out = new StringWriter();

    private int run(String... args) {
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(new StringWriter()));
        return command.execute(args);
    }

    @Test
    void theExampleHasNothingToDo() {
        assertThat(run("sources", "../examples/reseaux")).isZero();
        assertThat(out.toString()).contains("0 to take into account, 1 used, 0 ignored");
    }

    @Test
    void aNewDocumentComesWithItsEntryToPaste() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: T\nlang: fr\nsources: .\n");
        write("cursus/syllabus.yaml", "status: proposed\nchapters: []\n");
        write("cursus/sources.yaml", "- file: cours.md\n  status: used\n");
        write("cours.md", "cours");
        write("td 2.md", "td");

        assertThat(run("sources", dir.toString())).isZero();

        assertThat(out.toString())
                .contains("1 to take into account, 1 used", "new      td 2.md", "- file: \"td 2.md\"", "  sha256: ")
                .doesNotContain("subject.yaml", "syllabus.yaml");
    }

    private void write(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
