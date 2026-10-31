package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.cli.ExitCode;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * {@code boussole stop}: asks the server to stop; a server that does not answer any more is ended
 * through its process ID.
 */
@Command(name = "stop", mixinStandardHelpOptions = true, description = "Stop the server.")
public final class StopCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws InterruptedException {
        spec.commandLine().getOut().println(ServerControl.forCurrentUser().stop() ? "Server stopped." : "The server is not running.");
        return ExitCode.OK;
    }
}
