package io.github.lucasssoh.boussole.server;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * What the server is started with: its port (0 for any free one), the subject folders (asked
 * for on each request, so that a folder added to the configuration shows without a restart), the
 * folder of the built-in guide if it is installed, the progress database, the interface language
 * when the configuration sets one, and whether a lesson lets the learner go on without answering
 * its questions.
 */
public record ServerSettings(
        int port,
        Supplier<List<Path>> subjects,
        Optional<Path> guide,
        Path database,
        Optional<String> language,
        boolean freeNavigation) {

    /** The subject folders followed by the guide's, which is always there to come back to. */
    public List<Path> subjectsWithGuide() {
        return Stream.concat(subjects.get().stream(), guide.stream()).toList();
    }
}
