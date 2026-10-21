package io.github.lucasssoh.boussole.format.model.block;

import io.github.lucasssoh.boussole.format.model.SourceReference;
import java.util.Locale;
import java.util.Set;

/** A video file of the sources folder, such as the recording of a lecture. */
public record FileVideoSource(SourceReference file) implements VideoSource {

    /** The formats every current browser plays. */
    public static final Set<String> PLAYABLE = Set.of("mp4", "m4v", "webm", "ogv");

    public boolean playable() {
        String name = file.file().toLowerCase(Locale.ROOT);
        return PLAYABLE.stream().anyMatch(extension -> name.endsWith("." + extension));
    }
}
