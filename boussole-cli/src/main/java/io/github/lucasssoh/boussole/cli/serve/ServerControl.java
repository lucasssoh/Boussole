package io.github.lucasssoh.boussole.cli.serve;

import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The server of this computer, started in the background, stopped, opened in the browser: what
 * {@code open}, {@code stop} and {@code admin} do with it. The port is read from
 * {@code config.yaml} each time, so that a change of it is followed.
 */
public final class ServerControl {

    private static final Duration STARTUP = Duration.ofSeconds(40);
    private static final long GRACE_SECONDS = 5;

    private final BoussoleConfigFile config;
    private final BoussolePaths paths;

    public ServerControl(BoussoleConfigFile config, BoussolePaths paths) {
        this.config = config;
        this.paths = paths;
    }

    public static ServerControl forCurrentUser() {
        return new ServerControl(BoussoleConfigFile.forCurrentUser(), BoussolePaths.forCurrentUser());
    }

    public boolean isRunning() {
        return probe().isBoussole();
    }

    public String url(String path) {
        return probe().url(path);
    }

    /**
     * Starts the server in the background unless it runs already, and waits until it answers;
     * {@code starting} runs before the wait. Says whether it had to start it.
     */
    public boolean start(Runnable starting) throws IOException, InterruptedException {
        ServerProbe probe = probe();
        if (probe.isBoussole()) {
            return false;
        }
        if (probe.portTaken()) {
            throw new StartFailedException("port " + config.read().port() + " is taken by a program that does not answer as Boussole."
                    + " If it is a Boussole that stopped answering, boussole stop ends it; otherwise set another port: in " + config.path());
        }
        if (BackgroundServer.start(paths).isEmpty()) {
            throw new StartFailedException("start the server with boussole serve, then open " + probe.url(""));
        }
        starting.run();
        if (!probe.waitUntilUp(STARTUP)) {
            throw new StartFailedException("the server did not start; see " + paths.serverLog());
        }
        return true;
    }

    /**
     * Asks the server to stop; a server that does not answer any more is ended through its process
     * ID. Says whether one was running.
     */
    public boolean stop() throws InterruptedException {
        ServerProbe probe = probe();
        ServerPidFile pidFile = new ServerPidFile(paths.serverPid());
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
        return process.isPresent() || probe.portTaken();
    }

    /** Opens a page of the server in the browser; says whether a browser could be asked. */
    public boolean open(String path) {
        return BrowserOpener.open(url(path));
    }

    /** Opens a folder in the file manager. */
    public static boolean openFolder(Path folder) {
        return BrowserOpener.open(folder.toUri().toString());
    }

    ServerProbe probe() {
        return new ServerProbe(config.read().port());
    }

    private static boolean ended(ProcessHandle process) throws InterruptedException {
        try {
            process.onExit().get(GRACE_SECONDS, TimeUnit.SECONDS);
            return true;
        } catch (ExecutionException | TimeoutException e) {
            return !process.isAlive();
        }
    }

    /** Why the server could not be started, in a sentence that says what to do. */
    public static final class StartFailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        StartFailedException(String message) {
            super(message);
        }
    }
}
