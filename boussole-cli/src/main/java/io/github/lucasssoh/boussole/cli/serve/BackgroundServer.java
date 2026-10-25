package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.cli.Launcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Starts {@code boussole serve} as a process of its own, which outlives the terminal, its output
 * in a log file. A packaged Boussole starts its own program again. From boussole.jar, it runs a
 * copy of it: a running server reads its classes from its jar as it goes, and would break if a new
 * build replaced the file.
 */
final class BackgroundServer {

    private static final Path SETSID = Path.of("/usr/bin/setsid");

    private BackgroundServer() {
    }

    /** Starts the server; empty when Boussole does not run from its jar and cannot start a copy of itself. */
    static Optional<Process> start(BoussolePaths paths) throws IOException {
        Launcher launcher = Launcher.current();
        if (launcher.app().isEmpty() && launcher.jar().isEmpty()) {
            return Optional.empty();
        }
        Files.createDirectories(paths.state());
        List<String> command = new ArrayList<>();
        if (Files.isExecutable(SETSID)) {
            command.add(SETSID.toString());
        }
        if (launcher.app().isPresent()) {
            command.addAll(List.of(launcher.app().get().toString(), "serve"));
        } else {
            Files.copy(launcher.jar().get(), paths.serverJar(), StandardCopyOption.REPLACE_EXISTING);
            command.addAll(Launcher.commandLine(paths.serverJar(), "serve"));
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        // The program of a packaged Boussole marks its own process; a second one so marked would not start Boussole.
        builder.environment().keySet().removeIf(name -> name.startsWith("_JPACKAGE"));
        return Optional.of(builder
                .redirectInput(ProcessBuilder.Redirect.from(nullDevice()))
                .redirectOutput(ProcessBuilder.Redirect.appendTo(paths.serverLog().toFile()))
                .redirectErrorStream(true)
                .start());
    }

    private static File nullDevice() {
        return new File(System.getProperty("os.name", "").startsWith("Windows") ? "NUL" : "/dev/null");
    }
}
