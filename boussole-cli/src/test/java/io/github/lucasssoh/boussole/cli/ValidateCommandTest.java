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

class ValidateCommandTest {

    @TempDir
    Path dir;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private int run(String... args) {
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(err));
        return command.execute(args);
    }

    @Test
    void aValidSubjectSaysSo() {
        int status = run("validate", Path.of("..", "examples", "reseaux").toString());

        assertThat(status).isZero();
        assertThat(out.toString()).isEqualTo("../examples/reseaux: no problem, 2 of 2 sessions written.\n");
    }

    @Test
    void theOtherVersionsOfATrackAreComparedWithIt() throws IOException {
        Path guide = Path.of("..", "guide", "getting-started");
        copy(guide.resolve("en"), dir.resolve("guide/en"));
        copy(guide.resolve("fr"), dir.resolve("guide/fr"));
        Path session = dir.resolve("guide/fr/cursus/01-boussole/g1-ce-qu-est-boussole.md");
        Files.writeString(session, Files.readString(session).replace("{#roles}", "{#qui-fait-quoi}"));

        // Either version may be the one to fix: both say how they differ.
        assertThat(run("validate", dir.resolve("guide/en").toString())).isEqualTo(1);
        assertThat(run("validate", dir.resolve("guide/fr").toString())).isEqualTo(1);

        assertThat(out.toString()).contains("error F040 session `g1` has the notions [why, chain, qui-fait-quoi], the `en` version [why, chain, roles]");
    }

    private static void copy(Path from, Path to) throws IOException {
        try (var files = Files.walk(from)) {
            for (Path file : files.toList()) {
                Path target = to.resolve(from.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(file, target);
                }
            }
        }
    }

    @Test
    void problemsAreListedWithTheirPlace() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: T\nlang: fr\n");
        write("cursus/syllabus.yaml", """
                status: approved
                chapters:
                  - id: ch
                    title: Chapitre
                    dir: 01-ch
                    sessions:
                      - {id: s1, title: Un, duration: 60, goal: Faire., file: s1.md}
                      - {id: s2, title: Deux, duration: 60, goal: Faire., file: s2.md}
                """);
        write("cursus/01-ch/s1.md", "---\nid: s1\nobjectives: [Faire.]\nwritten_by:\n  - name: Test\n---\n:::exemple\n:::\n");

        int status = run("validate", dir.toString());

        assertThat(status).isEqualTo(1);
        assertThat(out.toString()).isEqualTo(dir + "/cursus/01-ch/s1.md:7: error F020 unknown directive `exemple`\n"
                + dir + "/cursus/syllabus.yaml:8: warning F011 session `s2` is not written yet: cursus/01-ch/s2.md\n"
                + dir + ": 1 error, 1 warning; 1 of 2 sessions written.\n");
    }

    @Test
    void warningsAloneDoNotFail() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: T\nlang: fr\n");
        write("cursus/syllabus.yaml", "status: approved\nchapters:\n  - {id: ch, title: C, dir: 01-ch, sessions: [{id: s1, title: Un, duration: 60, goal: Faire., file: s1.md}]}\n");

        assertThat(run("validate", "--errors-only", dir.toString())).isZero();
        assertThat(out.toString()).startsWith(dir.toString()).contains("0 errors, 1 warning");
    }

    @Test
    void aMissingFolderCannotBeRead() {
        assertThat(run("validate", dir.resolve("absent").toString())).isEqualTo(2);
        assertThat(err.toString()).contains("is not a folder");
    }

    private void write(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
