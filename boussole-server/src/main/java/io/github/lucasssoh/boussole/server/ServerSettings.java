package io.github.lucasssoh.boussole.server;

import io.github.lucasssoh.boussole.server.guide.BuiltInTrack;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What the server is started with: its port (0 for any free one), the subject folders (asked
 * for on each request, so that a folder added to the configuration shows without a restart), the
 * tracks built into Boussole as installed, the progress database, the interface language when
 * the configuration sets one, and whether a lesson lets the learner go on without answering its
 * questions.
 */
public record ServerSettings(
        int port,
        Supplier<List<Path>> subjects,
        List<BuiltInTrack> builtIn,
        Path database,
        Optional<String> language,
        boolean freeNavigation) {

    public ServerSettings {
        builtIn = List.copyOf(builtIn);
    }

    /** The subject folders followed by the built-in tracks, which are always there to come back to. */
    public List<Path> subjectsWithBuiltIn() {
        return Stream.concat(subjects.get().stream(), builtIn.stream().map(BuiltInTrack::folder)).toList();
    }

    public Set<String> builtInIds() {
        return builtIn.stream().map(BuiltInTrack::id).collect(Collectors.toUnmodifiableSet());
    }
}
