package io.github.lucasssoh.boussole.cli.init;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.cli.BoussoleCommand;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class InitCommandTest {

    @TempDir
    Path parent;

    private final StringWriter out = new StringWriter();

    private Path folder(String name) throws IOException {
        return Files.createDirectories(parent.resolve(name));
    }

    private int init(Path folder, String... options) {
        CommandLine command = new CommandLine(new BoussoleCommand());
        command.setOut(new PrintWriter(out));
        String[] args = new String[options.length + 2];
        args[0] = "init";
        System.arraycopy(options, 0, args, 1, options.length);
        args[args.length - 1] = folder.toString();
        return command.execute(args);
    }

    @Test
    void aFolderOfDocumentsBecomesAValidSubject() throws IOException {
        Path folder = folder("L&MC");
        Files.writeString(folder.resolve("Cours1.pdf"), "%PDF-1.4");

        assertThat(init(folder, "--lang", "fr")).isZero();

        assertThat(Files.readString(folder.resolve("subject.yaml")))
                .isEqualTo("format: 1\nid: l-mc\nname: \"L&MC\"\nlang: fr\nsources: \".\"\n");
        assertThat(new SubjectInspector().inspect(folder).diagnostics()).isEmpty();
        assertThat(folder.resolve("AGENTS.md")).content().contains("documents are in this folder: every file outside `cursus/`");
        assertThat(out.toString()).contains("Created  subject.yaml (id l-mc, lang fr, documents in .)", "cursus-plan");
    }

    @Test
    void theAgentKitIsInstalled() throws IOException {
        Path folder = folder("reseaux");
        init(folder, "--lang", "fr");

        AgentKit.SKILLS.forEach(skill -> assertThat(folder.resolve(".boussole/skills/" + skill + "/SKILL.md")).isRegularFile());
        assertThat(folder.resolve(".boussole/FORMAT.md")).content().startsWith("# Boussole course format");
        assertThat(folder.resolve("AGENTS.md")).content()
                .contains(MarkedBlock.START, "validate .", "documents are in `sources/`")
                .doesNotContain("{{");
        assertThat(folder.resolve("CLAUDE.md")).content().isEqualTo("@AGENTS.md\n");
    }

    @Test
    void documentsInASourcesFolderStayThere() throws IOException {
        Path folder = folder("OC");
        Files.createDirectories(folder.resolve("sources"));

        init(folder, "--lang", "fr", "--name", "Optimisation combinatoire");

        assertThat(folder.resolve("subject.yaml")).content().contains("id: oc", "name: \"Optimisation combinatoire\"", "sources: \"sources\"");
    }

    @Test
    void runningItAgainKeepsTheCourseAndTheLearnersText() throws IOException {
        Path folder = folder("reseaux");
        Files.writeString(folder.resolve("AGENTS.md"), "# Mes notes\n\nÀ garder.\n");
        Files.writeString(folder.resolve("CLAUDE.md"), "Réponds en français.\n");
        init(folder, "--lang", "fr");
        Files.writeString(folder.resolve("subject.yaml"), "format: 1\nid: reseaux\nname: Réseaux\nlang: fr\n");
        String agentsAfterFirstRun = Files.readString(folder.resolve("AGENTS.md"));

        init(folder, "--lang", "en");

        assertThat(folder.resolve("subject.yaml")).content().contains("name: Réseaux");
        assertThat(Files.readString(folder.resolve("AGENTS.md")))
                .isEqualTo(agentsAfterFirstRun)
                .startsWith("# Mes notes\n\nÀ garder.\n\n" + MarkedBlock.START);
        assertThat(folder.resolve("CLAUDE.md")).content().isEqualTo("@AGENTS.md\n\nRéponds en français.\n");
        assertThat(out.toString()).contains("Kept     subject.yaml", "Kept     CLAUDE.md");
    }

    @Test
    void theKitListsEverySkillOfTheRepository() throws IOException {
        try (var folders = Files.list(Path.of("..", "skills"))) {
            assertThat(folders.filter(Files::isDirectory).map(path -> path.getFileName().toString()))
                    .containsExactlyInAnyOrderElementsOf(AgentKit.SKILLS);
        }
    }
}
