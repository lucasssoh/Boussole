package io.github.lucasssoh.boussole.server.guide;

import java.nio.file.Path;

/** A track shipped inside Boussole, as installed: its subject ID and its folder. */
public record BuiltInTrack(String id, Path folder) {

    public boolean isStarter() {
        return id.equals(BundledTracks.STARTER);
    }
}
