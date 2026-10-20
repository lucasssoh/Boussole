package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.model.block.Block;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** A whole subject as read from its folder. */
public record Course(Path root, Subject subject, Syllabus syllabus, Lexicon lexicon, List<LoadedSession> sessions) {

    public Stream<Block> blocks() {
        return sessions.stream().flatMap(loaded -> loaded.session().blocks());
    }

    public SessionId sessionId(Chapter chapter, SessionEntry entry) {
        return new SessionId(subject.id(), chapter.id(), entry.id());
    }
}
