package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.diagnostic.Position;
import java.time.Duration;
import java.util.Optional;

/** A YouTube video shown beside the text, optionally limited to a segment. */
public record Video(
        String label, String youtubeId, Optional<Duration> start, Optional<Duration> end, Position position)
        implements Block {
}
