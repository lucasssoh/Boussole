package io.github.lucasssoh.boussole.cli;

import java.util.Optional;
import picocli.CommandLine.IVersionProvider;

/** The version written in the jar's manifest at build time. */
final class ManifestVersion implements IVersionProvider {

    @Override
    public String[] getVersion() {
        String version = Optional.ofNullable(BoussoleCommand.class.getPackage().getImplementationVersion()).orElse("dev");
        return new String[] {"Boussole " + version};
    }
}
