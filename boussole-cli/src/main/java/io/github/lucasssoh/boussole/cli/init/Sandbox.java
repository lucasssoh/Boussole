package io.github.lucasssoh.boussole.cli.init;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The sandbox of Claude Code, in which an agent of a track runs every command: it writes only in
 * the folder of the track and has no network. Any command is then allowed, a loop or a variable
 * included, so that an agent never takes a detour around a refused command.
 */
public final class Sandbox {

    /** The settings of Claude Code that turn it on, and never run a command outside it. */
    public static final String SETTINGS = "{\"sandbox\":{\"enabled\":true,\"autoAllowBashIfSandboxed\":true,\"allowUnsandboxedCommands\":false}}";

    private Sandbox() {
    }

    /**
     * Why the sandbox cannot run here, if it cannot: without it, commands would run outside it, so
     * no agent is started and no command is allowed. It runs on macOS, and on Linux (WSL included)
     * with {@code bwrap} and {@code socat}.
     */
    public static Optional<String> problem() {
        String system = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (system.contains("win")) {
            return Optional.of("the sandbox of Claude Code does not run on Windows: use Boussole in WSL");
        }
        if (!system.contains("linux")) {
            return Optional.empty();
        }
        String path = System.getenv().getOrDefault("PATH", "");
        List<String> missing = Stream.of("bwrap", "socat")
                .filter(program -> Stream.of(path.split(File.pathSeparator))
                        .noneMatch(dir -> !dir.isBlank() && Files.isExecutable(Path.of(dir, program))))
                .toList();
        return missing.isEmpty() ? Optional.empty()
                : Optional.of("install " + String.join(" and ", missing) + " (packages bubblewrap and socat) for the sandbox of Claude Code");
    }
}
