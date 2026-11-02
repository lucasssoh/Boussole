package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class LockCommandTest {

    private static final String SYLLABUS = """
            status: approved   # approved by the learner
            chapters:
              - id: tri
                title: Les tris
                dir: 01-tri
                sessions:
                  - id: s11
                    title: Tri par insertion
                    duration: 30
                    goal: Trier une liste par insertion.
                    file: 11-insertion.md
                  - id: e19
                    title: Examen blanc
                    kind: exam
                    duration: 60
                    goal: Réussir l'examen.
                    file: 19-examen.md
            """;

    @TempDir
    Path dir;

    private StringWriter out = new StringWriter();

    @BeforeEach
    void track() throws IOException {
        write("subject.yaml", "format: 1\nid: t\nname: Algo\nlang: fr\nsources: .\n");
        write("cursus/syllabus.yaml", SYLLABUS);
        write("cursus/sources.yaml", "- file: cours.md\n  status: used\n");
        write("cours.md", "cours");
    }

    private int run(String... args) {
        out = new StringWriter();
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(new StringWriter()));
        return command.execute(args);
    }

    @Test
    void lockingWritesTheDayUnderTheStatusAndKeepsTheRest() throws IOException {
        assertThat(run("lock", dir.toString())).isZero();

        assertThat(Files.readString(dir.resolve("cursus/syllabus.yaml")))
                .isEqualTo(SYLLABUS.replace("learner\n", "learner\nlocked: " + LocalDate.now() + "\n"));
        assertThat(out.toString()).contains("Algo locked", "The final mock exam (e19) can now be written.");
    }

    @Test
    void unlockingGivesTheFileBackAsItWas() throws IOException {
        run("lock", dir.toString());
        run("lock", dir.toString());

        assertThat(run("lock", dir.toString(), "--off")).isZero();

        assertThat(Files.readString(dir.resolve("cursus/syllabus.yaml"))).isEqualTo(SYLLABUS);
        assertThat(out.toString()).contains("Algo unlocked");
    }

    @Test
    void untilTheLockTheFinalMockExamWaitsAndAfterItNewDocumentsAreLeftOut() throws IOException {
        write("cours8.md", "cours 8");

        run("brief", dir.toString());
        assertThat(out.toString()).contains("the final mock exam (e19) waits", "e19    exam    60 min  waits",
                "Documents to take in: 1");

        run("lock", dir.toString());
        run("brief", dir.toString());
        assertThat(out.toString()).contains("Locked on " + LocalDate.now(), "e19    exam    60 min  to write",
                "Documents to take in: none", "Left out, the track being locked: cours8.md");
    }

    @Test
    void aFolderWithoutPlanCannotBeLocked() {
        assertThat(run("lock", dir.resolve("nothing").toString())).isEqualTo(ExitCode.UNUSABLE);
    }

    private void write(String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
