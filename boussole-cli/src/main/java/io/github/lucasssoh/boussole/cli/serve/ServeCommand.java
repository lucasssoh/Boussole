package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfig;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.server.BoussoleServer;
import io.github.lucasssoh.boussole.server.guide.BuiltInTrack;
import io.github.lucasssoh.boussole.server.guide.BundledTracks;
import java.util.List;
import java.nio.file.Path;
import io.github.lucasssoh.boussole.server.RunningServer;
import io.github.lucasssoh.boussole.server.ServerSettings;
import io.github.lucasssoh.boussole.server.NetworkSettings;
import io.github.lucasssoh.boussole.adapters.network.LocalCertificate;
import io.github.lucasssoh.boussole.adapters.network.NetworkAddresses;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/** {@code boussole serve}: runs the server in this terminal until Ctrl+C. */
@Command(
        name = "serve",
        mixinStandardHelpOptions = true,
        description = "Run the server in this terminal, until Ctrl+C. The subjects are those of config.yaml.")
public final class ServeCommand implements Callable<Integer> {

    @Option(names = "--port", description = "The port, instead of the one of config.yaml (default 7420).")
    private Integer port;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        BoussoleConfigFile config = BoussoleConfigFile.forCurrentUser();
        BoussoleConfig read = config.read();
        BoussolePaths paths = BoussolePaths.forCurrentUser();
        List<BuiltInTrack> builtIn = BundledTracks.install(paths.data().resolve("guide"));
        RunningServer server = BoussoleServer.start(new ServerSettings(
                port != null ? port : read.port(),
                () -> config.read().subjects(),
                builtIn,
                paths.database(),
                read.language(),
                read.freeNavigation(),
                read.network() ? Optional.of(network(read, paths)) : Optional.empty()));
        new ServerPidFile(BoussolePaths.forCurrentUser().serverPid()).writeCurrent();
        spec.commandLine().getOut().println("Boussole runs on " + server.url() + " (Ctrl+C to stop).");
        server.networkUrl().ifPresent(url -> spec.commandLine().getOut().println("Other devices: " + url + " (boussole devices pair)."));
        if (read.subjects().isEmpty()) {
            spec.commandLine().getOut().println("No track yet: start with the Getting started guide, then boussole open <folder> adds one.");
        }
        return 0;
    }

    /** The addresses and name other devices reach this computer by, and a certificate that names them all. */
    private static NetworkSettings network(BoussoleConfig read, BoussolePaths paths) {
        List<String> hosts = new ArrayList<>(NetworkAddresses.current());
        NetworkAddresses.localName().ifPresent(hosts::add);
        LocalCertificate certificate = LocalCertificate.ensure(paths.data().resolve("network"), hosts);
        return new NetworkSettings(read.networkPort(), certificate.keystore(), certificate.password(), hosts, certificate.fingerprint());
    }
}
