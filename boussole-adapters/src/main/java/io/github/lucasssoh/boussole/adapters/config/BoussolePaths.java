package io.github.lucasssoh.boussole.adapters.config;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Where Boussole keeps its own files for the current user: the XDG folders on Linux, and the
 * usual per-user folders on macOS and Windows.
 */
public record BoussolePaths(Path config, Path data, Path state) {

    private static final String NAME = "boussole";

    public static BoussolePaths forCurrentUser() {
        Path home = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            Path support = home.resolve("Library/Application Support/Boussole");
            return new BoussolePaths(support, support, home.resolve("Library/Logs/Boussole"));
        }
        if (os.contains("win")) {
            Path roaming = env("APPDATA").orElse(home.resolve("AppData/Roaming")).resolve("Boussole");
            Path local = env("LOCALAPPDATA").orElse(home.resolve("AppData/Local")).resolve("Boussole");
            return new BoussolePaths(roaming, local, local);
        }
        return new BoussolePaths(
                env("XDG_CONFIG_HOME").orElse(home.resolve(".config")).resolve(NAME),
                env("XDG_DATA_HOME").orElse(home.resolve(".local/share")).resolve(NAME),
                env("XDG_STATE_HOME").orElse(home.resolve(".local/state")).resolve(NAME));
    }

    public Path configFile() {
        return config.resolve("config.yaml");
    }

    /** The learner profile reused by every new subject. */
    public Path learnerProfile() {
        return config.resolve("learner.yaml");
    }

    public Path database() {
        return data.resolve("state.db");
    }

    public Path serverLog() {
        return state.resolve("server.log");
    }

    private static Optional<Path> env(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(value -> !value.isBlank()).map(Path::of);
    }
}
