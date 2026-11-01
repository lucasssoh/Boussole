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
        return app(columns, rows, RESEAUX);
    }

    private AdminApp app(int columns, int rows, Path track) throws IOException {
        Files.writeString(home.resolve("config.yaml"), "port: " + freePort() + "\nsubjects:\n  - " + track + "\n");
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
    void tab_and_shift_tab_go_round_the_screens() throws IOException {
        AdminApp app = app(100, 30);

        app.key(new KeyStroke(KeyType.Tab));
        assertThat(shown(app)).contains("Server stopped.");
        app.key(new KeyStroke(KeyType.ReverseTab));
        app.key(new KeyStroke(KeyType.ReverseTab));
        assertThat(shown(app)).contains("v Validate");
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
    void the_plan_an_agent_proposed_is_shown_then_approved_with_its_comments_kept() throws Exception {
        Path track = home.resolve("lmc");
        Files.createDirectories(track.resolve("cursus"));
        Files.writeString(track.resolve("subject.yaml"), "format: 1\nid: lmc\nname: Calcul\nlang: fr\nsources: .\n");
        Files.writeString(track.resolve("cursus/syllabus.yaml"), """
                # Plan proposé par l'agent.
                status: proposed   # à valider
                chapters:
                  - id: turing
                    title: Machines de Turing
                    dir: 01-turing
                    sessions:
                      - id: s11
                        title: Le 7-uplet
                        duration: 60
                        goal: Écrire le 7-uplet d'une machine.
                        file: 11-septuplet.md
                """);
        AdminApp app = app(120, 30, track);
        app.key(typed('3'));
        String shown = shown(app);
        for (int wait = 0; wait < 100 && !shown.contains("proposed"); wait++) {
            Thread.sleep(100);
            shown = shown(app);
        }

        app.key(typed('p'));
        assertThat(shown(app)).contains("Calcul · Plan", "1 chapter · 1 session · 1 h", "1. Machines de Turing", "s11", "Le 7-uplet",
                "a Approve");
        app.key(typed('a'));
        app.key(typed('y'));
        String syllabus = Files.readString(track.resolve("cursus/syllabus.yaml"));
        for (int wait = 0; wait < 100 && !shown(app).contains("approved."); wait++) {
            Thread.sleep(100);
        }

        assertThat(syllabus).startsWith("# Plan proposé par l'agent.\nstatus: approved # à valider\nchapters:");
        assertThat(shown(app)).contains("Approved: r starts an agent to write the 1 session left").doesNotContain("a Approve");
    }

    private static Path approvedTrack(Path folder) throws IOException {
        Files.createDirectories(folder.resolve("cursus"));
        Files.writeString(folder.resolve("subject.yaml"), "format: 1\nid: " + folder.getFileName() + "\nname: Calcul\nlang: fr\nsources: .\n");
        Files.writeString(folder.resolve("cursus/syllabus.yaml"), """
                status: approved
                chapters:
                  - id: turing
                    title: Machines de Turing
                    dir: 01-turing
                    sessions:
                      - {id: s11, title: Le 7-uplet, duration: 60, goal: Écrire le 7-uplet., file: 11-septuplet.md}
                """);
        return folder;
    }

    @Test
    void the_folders_beside_a_listed_track_that_hold_a_plan_are_shown_to_add() throws IOException {
        Path courses = Files.createDirectories(home.resolve("courses"));
        Path listed = approvedTrack(courses.resolve("listed"));
        approvedTrack(courses.resolve("beside"));
        Files.createDirectories(courses.resolve("no-plan"));
        Files.writeString(courses.resolve("no-plan/subject.yaml"), "format: 1\nid: no-plan\nname: Rien\nlang: fr\n");

        List<TracksTab.Track> read = TracksTab.read(List.of(listed));

        assertThat(read).extracting(track -> track.folder().getFileName().toString(), TracksTab.Track::listed)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("listed", true), org.assertj.core.groups.Tuple.tuple("beside", false));
    }

    @Test
    void r_starts_an_agent_on_an_approved_plan_and_the_screen_shows_its_tokens() throws Exception {
        Path track = approvedTrack(home.resolve("lmc"));
        Path agent = home.resolve("fake-claude");
        Files.writeString(agent, "#!/bin/sh\ncat <<'EOF'\n" + AgentRunTest.EVENTS + "EOF\n");
        agent.toFile().setExecutable(true);
        System.setProperty("boussole.agent", agent.toString());
        try {
            AdminApp app = app(140, 40, track);
            app.key(typed('3'));
            String shown = shown(app);
            for (int wait = 0; wait < 100 && !shown.contains("approved"); wait++) {
                Thread.sleep(100);
                shown = shown(app);
            }
            assertThat(shown).contains("r Run agent");

            app.key(typed('r'));
            app.key(typed('y'));
            for (int wait = 0; wait < 100 && !shown.contains("done 10k"); wait++) {
                Thread.sleep(100);
                shown = shown(app);
            }

            assertThat(shown).contains("done 10k", "Agent done in 2 min.", "Tokens 10k: 1k in, 2k out, 3k read from the cache, 4k written to it · $1.50 at list price",
                    "Sessions written.");
            assertThat(Files.list(track.resolve(".boussole/runs")).map(file -> file.getFileName().toString()))
                    .anyMatch(name -> name.endsWith(".jsonl")).anyMatch(name -> name.endsWith(".pid"));
        } finally {
            System.clearProperty("boussole.agent");
        }
    }

    @Test
    void j_and_k_scroll_the_plan_by_a_line_and_h_and_l_by_a_page() throws Exception {
        Path track = approvedTrack(home.resolve("long"));
        StringBuilder sessions = new StringBuilder("status: approved\nchapters:\n  - id: turing\n    title: Machines\n    dir: 01-turing\n    sessions:\n");
        for (int number = 10; number < 40; number++) {
            sessions.append("      - {id: s").append(number).append(", title: Séance ").append(number)
                    .append(", duration: 60, goal: Faire., file: s").append(number).append(".md}\n");
        }
        Files.writeString(track.resolve("cursus/syllabus.yaml"), sessions.toString());
        AdminApp app = app(120, 30, track);
        app.key(typed('3'));
        String shown = shown(app);
        for (int wait = 0; wait < 100 && !shown.contains("approved"); wait++) {
            Thread.sleep(100);
            shown = shown(app);
        }
        app.key(typed('p'));
        assertThat(shown(app)).contains("Approved: r starts", "hjkl Scroll");

        app.key(typed('j'));
        assertThat(shown(app)).doesNotContain("Approved: r starts").contains("1 chapter · 30 sessions");
        app.key(typed('k'));
        assertThat(shown(app)).contains("Approved: r starts");
        app.key(typed('l'));
        assertThat(shown(app)).doesNotContain("1 chapter · 30 sessions").contains("Séance 16");
        app.key(typed('h'));
        assertThat(shown(app)).contains("Approved: r starts");

        for (int press = 0; press < 20; press++) {
            app.key(typed('l'));
            shown(app);
        }
        assertThat(shown(app)).contains("Séance 39").doesNotContain("Séance 16 ");
        String atTheEnd = shown(app);
        app.key(typed('k'));
        assertThat(shown(app)).as("one k moves up at once, even after going past the end").isNotEqualTo(atTheEnd);
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
