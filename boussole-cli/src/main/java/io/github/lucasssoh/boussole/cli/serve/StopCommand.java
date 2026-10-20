package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.cli.ExitCode;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import java.io.IOException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/** {@code boussole stop}: stops the server started by {@code boussole open}. */
@Command(name = "stop", mixinStandardHelpOptions = true, description = "Stop the server.")
public final class StopCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws IOException, InterruptedException {
        ServerProbe probe = new ServerProbe(BoussoleConfigFile.forCurrentUser().read().port());
        if (!probe.isBoussole()) {
            spec.commandLine().getOut().println("The server is not running.");
            return ExitCode.OK;
        }
        probe.stop();
        spec.commandLine().getOut().println("Server stopped.");
        return ExitCode.OK;
    }
}
