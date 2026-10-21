package io.github.lucasssoh.boussole.cli.init;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.format.SubjectInspector;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class NewCommandTest {

    @TempDir
    Path home;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    private BoussoleConfigFile config() {
        return new BoussoleConfigFile(home.resolve(".config/boussole/config.yaml"), home);
    }

    private int run(String... args) {
        CommandLine command = new CommandLine(new NewCommand(config(), home));
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(err));
        String[] all = new String[args.length + 2];
        all[0] = "--profile";
        all[1] = home.resolve("no-profile.yaml").toString();
        System.arraycopy(args, 0, all, 2, args.length);
        return command.execute(all);
    }

    @Test
    void a_track_from_nothing_gets_its_folder_its_goal_and_a_place_in_boussole() throws IOException {
        assertThat(run("--lang", "fr", "--goal", "Écrire des scripts pour automatiser mon travail", "Python")).isZero();

        Path folder = home.resolve("Boussole/python");
        assertThat(folder.resolve("sources")).isDirectory();
        assertThat(Files.readString(folder.resolve("subject.yaml"))).isEqualTo(
                "format: 1\nid: python\nname: \"Python\"\nlang: fr\nsources: \"sources\"\n"
                        + "description: \"Écrire des scripts pour automatiser mon travail\"\n");
        assertThat(new SubjectInspector().inspect(folder).diagnostics()).extracting(Diagnostic::code).isEmpty();
        assertThat(config().read().subjects()).containsExactly(folder);
        assertThat(out.toString()).contains("Build this track: Écrire des scripts pour automatiser mon travail");
    }

    @Test
    void the_folder_of_tracks_can_be_set() throws IOException {
        Path elsewhere = home.resolve("Études");

        assertThat(run("--in", elsewhere.toString(), "Rust embarqué")).isZero();

        assertThat(elsewhere.resolve("rust-embarque/subject.yaml")).exists();
    }

    @Test
    void an_existing_track_is_never_overwritten() throws IOException {
        Path folder = Files.createDirectories(home.resolve("Boussole/python"));
        Files.writeString(folder.resolve("notes.md"), "mes notes");

        assertThat(run("Python")).isEqualTo(2);

        assertThat(folder.resolve("subject.yaml")).doesNotExist();
        assertThat(err.toString()).contains("already exists");
    }
}
