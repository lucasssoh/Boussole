package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.cli.Launcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Starts {@code boussole serve} as a process of its own, which outlives the terminal, its output in a log file. */
final class BackgroundServer {

    private static final Path SETSID = Path.of("/usr/bin/setsid");

    private BackgroundServer() {
    }

    /** Starts the server; empty when Boussole does not run from its jar and cannot start a copy of itself. */
    static Optional<Process> start(Path log) throws IOException {
        Optional<List<String>> serve = Launcher.current().commandLine("serve");
        if (serve.isEmpty()) {
            return Optional.empty();
        }
        List<String> command = new ArrayList<>();
        if (Files.isExecutable(SETSID)) {
            command.add(SETSID.toString());
        }
        command.addAll(serve.get());
        Files.createDirectories(log.getParent());
        return Optional.of(new ProcessBuilder(command)
                .redirectInput(ProcessBuilder.Redirect.from(nullDevice()))
                .redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile()))
                .redirectErrorStream(true)
                .start());
    }

    private static java.io.File nullDevice() {
        return new java.io.File(System.getProperty("os.name", "").startsWith("Windows") ? "NUL" : "/dev/null");
    }
}
