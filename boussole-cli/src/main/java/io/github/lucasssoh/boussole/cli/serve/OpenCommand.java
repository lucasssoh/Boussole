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
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
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
            "With a subject folder, open that subject, adding it to config.yaml the first time."
        })
public final class OpenCommand implements Callable<Integer> {

    private static final Duration STARTUP = Duration.ofSeconds(40);

    @Parameters(paramLabel = "FOLDER", arity = "0..1", description = "A subject folder, the one holding subject.yaml.")
    private Path folder;

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
        ServerProbe probe = new ServerProbe(config.read().port());
        if (!probe.isBoussole()) {
            if (probe.portTaken()) {
                err.println("boussole: another program uses port " + config.read().port() + "; set another one with port: in " + config.path());
                return ExitCode.UNUSABLE;
            }
            Path log = BoussolePaths.forCurrentUser().serverLog();
            if (BackgroundServer.start(log).isEmpty()) {
                err.println("boussole: start the server with boussole serve, then open " + probe.url(""));
                return ExitCode.UNUSABLE;
            }
            out.println("Starting the server…");
            if (!probe.waitUntilUp(STARTUP)) {
                err.println("boussole: the server did not start; see " + log);
                return ExitCode.UNUSABLE;
            }
        }
        String url = probe.url(subject.map(id -> "subjects/" + id).orElse(""));
        out.println((BrowserOpener.open(url) ? "Opened " : "Open ") + url);
        return ExitCode.OK;
    }
}
