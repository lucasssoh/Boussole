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

class BriefCommandTest {

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
    void theExampleSaysItsChaptersSessionsAndNotionsWithNothingToTakeIn() {
        assertThat(run("brief", "../examples/reseaux")).isZero();

        assertThat(out.toString())
                .contains("Track reseaux", "plan approved", "Documents to take in: none",
                        "couche-reseau  \"La couche réseau\"  cursus/01-couche-reseau",
                        "documents: Reseaux_Cours_Intro_2026.pdf", "s11", "written", "notions: ", "Lexicon: ")
                .doesNotContain("to write");
    }

    @Test
    void aNewDocumentAndASessionLeftAreSaid() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: T\nlang: fr\nsources: .\n");
        write("cursus/syllabus.yaml", """
                status: approved
                chapters:
                  - id: tri
                    title: Les tris
                    dir: 01-tri
                    sources: [cours.md]
                    sessions:
                      - id: s11
                        title: Tri par insertion
                        duration: 30
                        goal: Trier une liste par insertion.
                        file: 11-insertion.md
                """);
        write("cursus/sources.yaml", "- file: cours.md\n  status: used\n- file: annales.md\n  status: pending\n");
        write("cours.md", "cours");
        write("annales.md", "annales");
        write("graphes.md", "graphes");

        assertThat(run("brief", dir.toString())).isZero();

        assertThat(out.toString())
                .contains("Documents to take in: 1", "new     graphes.md  sha256: ", "Pending, by choice: annales.md",
                        "s11    lesson  30 min  to write Tri par insertion");
    }

    @Test
    void aFolderWithoutSubjectIsRefused() {
        assertThat(run("brief", dir.toString())).isEqualTo(ExitCode.UNUSABLE);
    }

    private void write(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
