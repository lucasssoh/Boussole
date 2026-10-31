package io.github.lucasssoh.boussole.cli.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.adapters.config.BoussolePaths;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** {@code boussole} alone drawn in a terminal of memory, with no server running. */
class AdminAppTest {

    private static final Path RESEAUX = Path.of("../examples/reseaux").toAbsolutePath().normalize();

    @TempDir
    Path home;

    private TerminalScreen screen;
    private BoussoleConfigFile config;

    private AdminApp app(int columns, int rows) throws IOException {
        Files.writeString(home.resolve("config.yaml"), "port: " + freePort() + "\nsubjects:\n  - " + RESEAUX + "\n");
        config = new BoussoleConfigFile(home.resolve("config.yaml"), home);
        screen = new TerminalScreen(new DefaultVirtualTerminal(new TerminalSize(columns, rows)));
        screen.startScreen();
        BoussolePaths paths = new BoussolePaths(home.resolve("config"), home.resolve("data"), home.resolve("state"));
        return new AdminApp(screen, config, new ServerControl(config, paths), "test");
    }

    private String shown(AdminApp app) throws IOException {
        app.refresh();
        app.draw();
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < screen.getTerminalSize().getRows(); row++) {
            for (int column = 0; column < screen.getTerminalSize().getColumns(); column++) {
                text.append(screen.getFrontCharacter(column, row).getCharacterString());
            }
            text.append('\n');
        }
        return text.toString();
    }

    private static KeyStroke typed(char character) {
        return new KeyStroke(character, false, false);
    }

    @Test
    void the_server_screen_says_it_is_stopped_and_what_the_configuration_says() throws IOException {
        AdminApp app = app(100, 30);

        String shown = shown(app);

        assertThat(shown).contains("○ Stopped", " F1 Server ", " F2 Devices ", " F3 Tracks ", "Configuration", "Network       off",
                "Tracks        1", "s Start");
    }

    @Test
    void the_devices_screen_waits_for_the_server() throws IOException {
        AdminApp app = app(100, 30);

        app.key(new KeyStroke(KeyType.F2));

        assertThat(shown(app)).contains("Server stopped.", "a Pair");
    }

    @Test
    void the_tracks_screen_validates_each_track_and_lists_its_documents() throws Exception {
        AdminApp app = app(120, 30);

        app.key(typed('3'));
        String shown = shown(app);
        for (int wait = 0; wait < 100 && !shown.contains("up to date") && !shown.contains("to do"); wait++) {
            Thread.sleep(100);
            shown = shown(app);
        }

        assertThat(shown).contains("examples/reseaux", "Documents", "v Validate");
        assertThat(TracksTab.read(List.of(RESEAUX)).getFirst().course()).isPresent();
    }

    @Test
    void a_folder_without_a_subject_is_not_added() throws IOException {
        AdminApp app = app(100, 30);
        app.key(typed('3'));
        app.key(typed('+'));
        for (char character : "/nowhere".toCharArray()) {
            app.key(typed(character));
        }

        assertThat(shown(app)).contains("Folder: ~//nowhere");
        app.key(new KeyStroke(KeyType.Backspace));
        app.key(new KeyStroke(KeyType.Enter));

        assertThat(shown(app)).contains("no subject.yaml");
        assertThat(config.read().subjects()).containsExactly(RESEAUX);
    }

    @Test
    void a_terminal_too_small_says_the_size_it_needs() throws IOException {
        AdminApp app = app(60, 20);

        assertThat(shown(app)).contains("Terminal 60×20, 80×24 at least.");
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
