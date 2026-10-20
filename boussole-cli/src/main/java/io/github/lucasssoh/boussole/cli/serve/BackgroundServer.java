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
 * in a log file. It runs a copy of boussole.jar: a running server reads its classes from its jar
 * as it goes, and would break if a new build replaced the file.
 */
final class BackgroundServer {

    private static final Path SETSID = Path.of("/usr/bin/setsid");

    private BackgroundServer() {
    }

    /** Starts the server; empty when Boussole does not run from its jar and cannot start a copy of itself. */
    static Optional<Process> start(BoussolePaths paths) throws IOException {
        Optional<Path> jar = Launcher.current().jar();
        if (jar.isEmpty()) {
            return Optional.empty();
        }
        Files.createDirectories(paths.state());
        Files.copy(jar.get(), paths.serverJar(), StandardCopyOption.REPLACE_EXISTING);
        List<String> command = new ArrayList<>();
        if (Files.isExecutable(SETSID)) {
            command.add(SETSID.toString());
        }
        command.addAll(Launcher.commandLine(paths.serverJar(), "serve"));
        return Optional.of(new ProcessBuilder(command)
                .redirectInput(ProcessBuilder.Redirect.from(nullDevice()))
                .redirectOutput(ProcessBuilder.Redirect.appendTo(paths.serverLog().toFile()))
                .redirectErrorStream(true)
                .start());
    }

    private static File nullDevice() {
        return new File(System.getProperty("os.name", "").startsWith("Windows") ? "NUL" : "/dev/null");
    }
}
