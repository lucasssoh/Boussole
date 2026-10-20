package io.github.lucasssoh.boussole.server;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * What the server is started with: its port (0 for any free one), the subject folders (asked
 * for on each request, so that a folder added to the configuration shows without a restart), the
 * progress database and the interface language when the configuration sets one.
 */
public record ServerSettings(int port, Supplier<List<Path>> subjects, Path database, Optional<String> language) {
}
