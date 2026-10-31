package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.cli.ExitCode;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.load.CourseLoader;
import io.github.lucasssoh.boussole.format.model.Course;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * {@code boussole open [FOLDER]}: starts the server in the background if it is not running, then
 * opens the browser, on the subject of FOLDER when one is given. A folder not yet in
 * {@code config.yaml} is added to it.
 */
@Command(
        name = "open",
        mixinStandardHelpOptions = true,
        description = {
            "Open Boussole in the browser, starting the server in the background if needed.",
            "With a subject folder, open that subject, adding it to config.yaml the first time;",
            "with --resume, open its session to resume, the one in progress or else the next one."
        })
public final class OpenCommand implements Callable<Integer> {

    @Parameters(paramLabel = "FOLDER", arity = "0..1", description = "A subject folder, the one holding subject.yaml.")
    private Path folder;

    @Option(names = "--resume", description = "Open the session to resume of the subject, rather than its page: for a reminder that starts a session.")
    private boolean resume;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws IOException, InterruptedException {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();
        BoussoleConfigFile config = BoussoleConfigFile.forCurrentUser();
        Optional<String> subject = Optional.empty();
        if (folder != null) {
            Optional<Course> course = Files.isRegularFile(folder.resolve("subject.yaml"))
                    ? CourseLoader.load(folder, new DiagnosticCollector())
                    : Optional.empty();
            if (course.isEmpty()) {
                err.println("boussole: " + folder + " has no readable subject.yaml (boussole init " + folder + " creates one)");
                return ExitCode.UNUSABLE;
            }
            subject = Optional.of(course.get().subject().id());
            if (config.addSubject(folder)) {
                out.println("Added " + folder.toAbsolutePath().normalize() + " to " + config.path() + ".");
            }
        }
        ServerControl server = new ServerControl(config, BoussolePaths.forCurrentUser());
        try {
            server.start(() -> {
                out.println("Starting the server…");
                out.flush();
            });
        } catch (ServerControl.StartFailedException e) {
            err.println("boussole: " + e.getMessage());
            return ExitCode.UNUSABLE;
        }
        ServerProbe probe = server.probe();
        String url = probe.url(subject.map(id -> "subjects/" + id
                + (resume ? probe.sessionToResume(id).map(session -> "/sessions/" + session).orElse("") : "")).orElse(""));
        out.println((BrowserOpener.open(url) ? "Opened " : "Open ") + url);
        return ExitCode.OK;
    }
}
