package io.github.lucasssoh.boussole.server.api;

import java.util.Optional;

/** The version of the running Boussole, written in the jar's manifest when it was built. */
final class BoussoleVersion {

    private BoussoleVersion() {
    }

    static String current() {
        return Optional.ofNullable(BoussoleVersion.class.getPackage().getImplementationVersion()).orElse("dev");
    }
}
