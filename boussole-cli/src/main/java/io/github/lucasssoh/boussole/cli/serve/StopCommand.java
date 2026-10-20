package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * {@code boussole stop}: asks the server to stop; a server that does not answer any more is ended
 * through its process ID.
 */
@Command(name = "stop", mixinStandardHelpOptions = true, description = "Stop the server.")
public final class StopCommand implements Callable<Integer> {

    private static final long GRACE_SECONDS = 5;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws InterruptedException {
        PrintWriter out = spec.commandLine().getOut();
        ServerProbe probe = new ServerProbe(BoussoleConfigFile.forCurrentUser().read().port());
        ServerPidFile pidFile = new ServerPidFile(BoussolePaths.forCurrentUser().serverPid());
        Optional<ProcessHandle> process = pidFile.server();
        if (probe.isBoussole()) {
            try {
                probe.stop();
            } catch (IOException e) {
                // It may stop while answering; the process is checked below.
            }
        }
        if (process.isPresent() && !ended(process.get())) {
            process.get().destroy();
            if (!ended(process.get())) {
                process.get().destroyForcibly();
            }
        }
        pidFile.delete();
        if (process.isEmpty() && !probe.portTaken()) {
            out.println("The server is not running.");
        } else {
            out.println("Server stopped.");
        }
        return ExitCode.OK;
    }

    private static boolean ended(ProcessHandle process) throws InterruptedException {
        try {
            process.onExit().get(GRACE_SECONDS, TimeUnit.SECONDS);
            return true;
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            return !process.isAlive();
        }
    }
}
