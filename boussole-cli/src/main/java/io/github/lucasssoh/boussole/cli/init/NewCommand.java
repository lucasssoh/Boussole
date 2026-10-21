package io.github.lucasssoh.boussole.cli.init;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.Launcher;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole new NAME}: a track to learn something without documents of one's own. Creates
 * its folder (in {@code tracks_dir}, {@code ~/Boussole} by default), prepares it as
 * {@code boussole init} does, and adds it to the tracks Boussole shows. The agent then finds the
 * sources itself (the {@code cursus-discover} playbook).
 */
@Command(
        name = "new",
        mixinStandardHelpOptions = true,
        description = {
            "Start a track from nothing: Boussole creates its folder and prepares it for an AI agent,",
            "which asks what you want to learn, finds open sources, and proposes a plan to approve."
        })
public final class NewCommand implements Callable<Integer> {

    @Parameters(paramLabel = "NAME", description = "What to learn, as the track's name: \"Python\", \"Rust for embedded\"…")
    private String name;

    @Option(names = "--goal", description = "What you want to be able to do, in your words; the agent starts from it.", defaultValue = "")
    private String goal;

    @Option(names = "--lang", description = "Language of the track, as en, fr… (default: the system's).")
    private String lang = Locale.getDefault().getLanguage();

    @Option(names = "--in", paramLabel = "FOLDER", description = "Where to create the track's folder (default: tracks_dir of config.yaml, else ~/Boussole).")
    private Path parent;

    @Option(names = "--profile", paramLabel = "FILE", description = "The learner's usual profile (default: ${DEFAULT-VALUE}).")
    private Path profile = LearnerProfiles.usual();

    @Spec
    private CommandSpec spec;

    private final BoussoleConfigFile config;
    private final Path home;

    public NewCommand() {
        this(BoussoleConfigFile.forCurrentUser(), Path.of(System.getProperty("user.home")));
    }

    /** For tests, which must never touch the learner's own configuration. */
    NewCommand(BoussoleConfigFile config, Path home) {
        this.config = config;
        this.home = home;
    }

    @Override
    public Integer call() {
        Path where = parent != null ? parent : config.read().tracksDir().orElse(home.resolve("Boussole"));
        Path folder = where.resolve(Slug.of(name)).toAbsolutePath().normalize();
        if (Files.exists(folder) && !isEmptyFolder(folder)) {
            spec.commandLine().getErr().println("boussole: " + folder + " already exists; boussole init " + folder + " prepares it as it is");
            return 2;
        }
        try {
            Files.createDirectories(folder.resolve("sources"));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot create " + folder, e);
        }
        SubjectFolder subject = new SubjectFolder(folder);
        SubjectScaffold scaffold = new SubjectScaffold(subject);
        InitReport report = new InitReport();
        scaffold.create(new SubjectScaffold.Settings(Slug.of(name), name, lang, "sources", goal), report);
        scaffold.copyProfile(profile, report);
        new AgentKitInstaller(subject).install("sources", Launcher.current().validateCommand(), report);
        config.addSubject(folder);

        var out = spec.commandLine().getOut();
        report.lines().forEach(out::println);
        out.println();
        out.println("The track " + name + " is ready in " + folder + ", and listed in Boussole.");
        out.println("Next: open an AI agent in that folder and ask it: \"Build this track"
                + (goal.isBlank() ? "" : ": " + goal.strip()) + "\".");
        out.println("It asks what you want to learn, finds open sources and cites them, then proposes a plan for you to approve.");
        out.flush();
        return 0;
    }

    private static boolean isEmptyFolder(Path folder) {
        try (Stream<Path> entries = Files.list(folder)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
}
