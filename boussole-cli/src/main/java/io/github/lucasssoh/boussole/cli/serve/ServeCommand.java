package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfig;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.server.BoussoleServer;
import io.github.lucasssoh.boussole.server.RunningServer;
import io.github.lucasssoh.boussole.server.ServerSettings;
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
        RunningServer server = BoussoleServer.start(new ServerSettings(
                port != null ? port : read.port(),
                () -> config.read().subjects(),
                BoussolePaths.forCurrentUser().database(),
                read.language(),
                read.freeNavigation()));
        new ServerPidFile(BoussolePaths.forCurrentUser().serverPid()).writeCurrent();
        spec.commandLine().getOut().println("Boussole runs on " + server.url() + " (Ctrl+C to stop).");
        if (read.subjects().isEmpty()) {
            spec.commandLine().getOut().println("No subject yet: boussole open <folder> adds one.");
        }
        return 0;
    }
}
