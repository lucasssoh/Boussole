package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.time.Duration;
import java.util.Optional;

/** A YouTube video shown beside the text, optionally limited to a segment. */
public record VideoBlock(
        String label, String youtubeId, Optional<Duration> start, Optional<Duration> end, SourcePosition position)
        implements Block {
}
