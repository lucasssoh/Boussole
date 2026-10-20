package io.github.lucasssoh.boussole.adapters.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * What {@code config.yaml} says: the interface language, the port, the subject folders, and
 * whether the learner may move freely through a lesson ({@code free_navigation}) instead of
 * answering each question before going on.
 */
public record BoussoleConfig(Optional<String> language, int port, List<Path> subjects, boolean freeNavigation) {

    public static final int DEFAULT_PORT = 7420;

    public BoussoleConfig {
        subjects = List.copyOf(subjects);
    }

    public static BoussoleConfig defaults() {
        return new BoussoleConfig(Optional.empty(), DEFAULT_PORT, List.of(), false);
    }
}
