package io.github.lucasssoh.boussole.adapters.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * What {@code config.yaml} says: the interface language, the port, the subject folders, whether
 * the learner may move freely through a lesson ({@code free_navigation}) instead of answering
 * each question before going on, where {@code boussole new} creates tracks ({@code tracks_dir}),
 * and whether other devices of the local network may reach it ({@code network}, over HTTPS on
 * {@code network_port}).
 */
public record BoussoleConfig(Optional<String> language, int port, List<Path> subjects, boolean freeNavigation, Optional<Path> tracksDir,
        boolean network, int networkPort) {

    public static final int DEFAULT_PORT = 7420;
    public static final int DEFAULT_NETWORK_PORT = 7421;

    public BoussoleConfig {
        subjects = List.copyOf(subjects);
    }

    public static BoussoleConfig defaults() {
        return new BoussoleConfig(Optional.empty(), DEFAULT_PORT, List.of(), false, Optional.empty(), false, DEFAULT_NETWORK_PORT);
    }
}
