package io.github.lucasssoh.boussole.adapters.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** What {@code config.yaml} says: the interface language, the port and the subject folders. */
public record BoussoleConfig(Optional<String> language, int port, List<Path> subjects) {

    public static final int DEFAULT_PORT = 7420;

    public BoussoleConfig {
        subjects = List.copyOf(subjects);
    }

    public static BoussoleConfig defaults() {
        return new BoussoleConfig(Optional.empty(), DEFAULT_PORT, List.of());
    }
}
