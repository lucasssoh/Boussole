package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.SourcePosition;
import java.time.Duration;
import java.util.Optional;

/** A video shown beside the text, from YouTube or from the sources folder, optionally limited to a segment. */
public record VideoBlock(
        String label, VideoSource source, Optional<Duration> start, Optional<Duration> end, SourcePosition position)
        implements Block {
}
