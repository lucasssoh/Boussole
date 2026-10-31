package io.github.lucasssoh.boussole.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * How this Boussole was started, so that instructions written for agents name a command that
 * runs this very version, even when another program is called {@code boussole}, and so that
 * {@code boussole open} can start the server the same way: from a packaged Boussole (its own
 * program, its own Java inside, {@code app}), or from {@code boussole.jar} ({@code jar}). The
 * {@code boussole.jar} property names the jar when a copy of it runs ({@code bin/boussole}).
 */
public record Launcher(Optional<Path> app, Optional<Path> jar) {

    public static Launcher current() {
        Optional<Path> app = Optional.ofNullable(System.getProperty("jpackage.app-path")).map(path -> Path.of(path).toAbsolutePath());
        String classPath = System.getProperty("java.class.path", "");
        boolean singleJar = !classPath.contains(java.io.File.pathSeparator) && classPath.endsWith(".jar");
        Optional<Path> jar = Optional.ofNullable(System.getProperty("boussole.jar")).map(Path::of)
                .or(() -> singleJar ? Optional.of(Path.of(classPath)) : Optional.empty())
                .map(Path::toAbsolutePath);
        return new Launcher(app, jar);
    }

    public String validateCommand() {
        return app.map(path -> quoted(path.toString()) + " validate .")
                .or(() -> jar.map(path -> "java -jar " + quoted(path.toString()) + " validate ."))
                .orElse("boussole validate .");
    }

    /** The command line that runs a subcommand of the given jar in a new process, with the same Java. */
    public static List<String> commandLine(Path jar, String... arguments) {
        Path home = Path.of(System.getProperty("java.home", ""), "bin", isWindows() ? "java.exe" : "java");
        String java = Files.isExecutable(home) ? home.toString() : ProcessHandle.current().info().command().orElse("java");
        List<String> command = new java.util.ArrayList<>(List.of(java, "-jar", jar.toString()));
        command.addAll(List.of(arguments));
        return List.copyOf(command);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").startsWith("Windows");
    }

    private static String quoted(String path) {
        return path.chars().anyMatch(Character::isWhitespace) ? "\"" + path + "\"" : path;
    }
}
