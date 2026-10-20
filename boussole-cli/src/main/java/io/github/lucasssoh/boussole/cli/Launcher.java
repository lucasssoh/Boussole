package io.github.lucasssoh.boussole.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * How this Boussole was started, so that instructions written for agents name a command that
 * runs this very version, even when another program is called {@code boussole}, and so that
 * {@code boussole open} can start the server the same way.
 */
public record Launcher(Optional<Path> jar) {

    public static Launcher current() {
        String classPath = System.getProperty("java.class.path", "");
        boolean singleJar = !classPath.contains(java.io.File.pathSeparator) && classPath.endsWith(".jar");
        return new Launcher(singleJar ? Optional.of(Path.of(classPath).toAbsolutePath()) : Optional.empty());
    }

    public String validateCommand() {
        return jar.map(path -> "java -jar " + quoted(path.toString()) + " validate .").orElse("boussole validate .");
    }

    /** The command line that runs a subcommand of the given jar in a new process, with the same Java. */
    public static List<String> commandLine(Path jar, String... arguments) {
        String java = ProcessHandle.current().info().command().orElse("java");
        List<String> command = new java.util.ArrayList<>(List.of(java, "-jar", jar.toString()));
        command.addAll(List.of(arguments));
        return List.copyOf(command);
    }

    private static String quoted(String path) {
        return path.chars().anyMatch(Character::isWhitespace) ? "\"" + path + "\"" : path;
    }
}
