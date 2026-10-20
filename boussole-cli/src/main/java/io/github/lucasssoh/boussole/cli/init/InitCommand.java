package io.github.lucasssoh.boussole.cli.init;

import io.github.lucasssoh.boussole.cli.Launcher;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole init FOLDER}: makes a folder of course documents a Boussole subject, ready for an agent. */
@Command(
        name = "init",
        mixinStandardHelpOptions = true,
        description = {
            "Make a folder of course documents a Boussole subject, ready for an AI agent to build the course.",
            "Creates subject.yaml and an empty syllabus if missing, and installs the agent skills,",
            "the course format, AGENTS.md and CLAUDE.md. Run it again to update the skills;",
            "it never overwrites the course nor the learner's documents."
        })
public final class InitCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", description = "The folder holding the course documents.")
    private Path folder;

    @Option(names = "--id", description = "ID of the subject (default: from the folder name).", defaultValue = "")
    private String id;

    @Option(names = "--name", description = "Name of the subject (default: the folder name).", defaultValue = "")
    private String name;

    @Option(names = "--lang", description = "Language of the documents, as en, fr… (default: the system's).")
    private String lang = Locale.getDefault().getLanguage();

    @Option(names = "--profile", paramLabel = "FILE", description = "The learner's usual profile, copied when the subject has no learner.yaml (default: ${DEFAULT-VALUE}).")
    private Path profile = LearnerProfiles.usual();

    @Option(names = "--sources", description = "Folder of the documents, relative to FOLDER (default: sources/ if it exists, else FOLDER itself when it holds PDFs).", defaultValue = "")
    private String sources;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        if (!Files.isDirectory(folder)) {
            spec.commandLine().getErr().println("boussole: " + folder + " is not a folder");
            return 2;
        }
        SubjectFolder subject = new SubjectFolder(folder);
        SubjectScaffold scaffold = new SubjectScaffold(subject);
        InitReport report = new InitReport();
        scaffold.create(new SubjectScaffold.Settings(id, name, lang, sources), report);
        scaffold.copyProfile(profile, report);
        new AgentKitInstaller(subject).install(scaffold.sources(sources), Launcher.current().validateCommand(), report);

        var out = spec.commandLine().getOut();
        report.lines().forEach(out::println);
        out.println();
        out.println("Next: open an AI agent in " + folder + " and ask it to build the course.");
        out.println("It first asks how you learn best (unless learner.yaml exists), then proposes a plan for you to approve.");
        out.flush();
        return 0;
    }
}
