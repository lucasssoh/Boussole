package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import java.io.Console;
import java.io.IOException;
import java.util.Optional;

/** {@code boussole} alone: the server, the devices and the tracks, full screen in the terminal (beta). */
public final class AdminLauncher {

    private AdminLauncher() {
    }

    /** Whether there is a terminal to draw in; without one, {@code boussole} prints its help. */
    public static boolean hasTerminal() {
        Console console = System.console();
        return console != null && console.isTerminal();
    }

    public static void run() throws IOException, InterruptedException {
        BoussoleConfigFile config = BoussoleConfigFile.forCurrentUser();
        config.read();
        String version = Optional.ofNullable(AdminLauncher.class.getPackage().getImplementationVersion()).orElse("dev");
        TerminalScreen screen = new TerminalScreen(new DefaultTerminalFactory().setForceTextTerminal(true).createTerminal());
        new AdminApp(screen, config, ServerControl.forCurrentUser(), version).run();
    }
}
