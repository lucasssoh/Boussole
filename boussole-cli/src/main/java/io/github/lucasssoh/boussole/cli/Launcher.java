package io.github.lucasssoh.boussole.cli;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * How this Boussole was started, so that instructions written for agents name a command that
 * runs this very version, even when another program is called {@code boussole}.
 */
public record Launcher(Optional<Path> jar) {

    public static Launcher current() {
        try {
            Path location = Path.of(BoussoleCommand.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new Launcher(location.toString().endsWith(".jar") ? Optional.of(location.toAbsolutePath()) : Optional.empty());
        } catch (URISyntaxException | SecurityException e) {
            return new Launcher(Optional.empty());
        }
    }

    public String validateCommand() {
        return jar.map(path -> "java -jar " + quoted(path.toString()) + " validate .").orElse("boussole validate .");
    }

    private static String quoted(String path) {
        return path.chars().anyMatch(Character::isWhitespace) ? "\"" + path + "\"" : path;
    }
}
