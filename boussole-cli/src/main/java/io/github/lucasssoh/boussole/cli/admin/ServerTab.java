package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfig;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.admin.Painter.Style;
import io.github.lucasssoh.boussole.cli.devices.DeviceCommandException;
import io.github.lucasssoh.boussole.cli.devices.LocalServer;
import io.github.lucasssoh.boussole.cli.serve.ServerControl;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/** F1: whether the server runs, where it answers, what {@code config.yaml} says; start, stop, network. */
final class ServerTab implements AdminTab {

    private final AdminApp app;
    private final BoussoleConfigFile config;
    private final ServerControl control;
    private volatile Status status = Status.STOPPED;

    ServerTab(AdminApp app, BoussoleConfigFile config, ServerControl control) {
        this.app = app;
        this.config = config;
        this.control = control;
    }

    /** {@code network}: what the server says of its network access, null when it does not say. */
    private record Status(boolean running, String version, JsonNode network, int devices, int requests) {

        static final Status STOPPED = new Status(false, "", null, 0, 0);
    }

    @Override
    public String name() {
        return "Server";
    }

    boolean running() {
        return status.running();
    }

    @Override
    public void refresh() {
        if (!control.isRunning()) {
            status = Status.STOPPED;
            return;
        }
        LocalServer server = LocalServer.on(config.read().port());
        try {
            String version = server.get("/api/server").path("version").asText("");
            try {
                JsonNode all = server.get("/api/devices");
                status = new Status(true, version, all.path("network"), all.path("devices").size(), all.path("requests").size());
            } catch (DeviceCommandException e) {
                status = new Status(true, version, null, 0, 0);
            }
        } catch (DeviceCommandException e) {
            status = Status.STOPPED;
        }
    }

    @Override
    public void draw(Painter painter, int top, int height) {
        BoussoleConfig read = config.read();
        Status shown = status;
        int half = painter.columns() / 2;
        int width = half - 4;
        painter.pane(0, top, half, height, "Status", false);
        int row = top + 1;
        painter.field(2, row++, width, "State", shown.running() ? "Running" : "Stopped", shown.running() ? Style.GOOD : Style.DIM);
        if (shown.running()) {
            painter.field(2, row++, width, "Version", shown.version(), Style.PLAIN);
        }
        painter.field(2, row++, width, "Local", control.url(""), Style.PLAIN);
        String label = "Network";
        for (String line : network(shown, read)) {
            painter.field(2, row++, width, label, line, Style.PLAIN);
            label = "";
        }
        if (shown.network() != null && shown.network().path("enabled").asBoolean()) {
            painter.field(2, row++, width, "Fingerprint", shown.network().path("fingerprint").asText(), Style.PLAIN);
        }
        painter.field(2, row++, width, "Tracks", String.valueOf(read.subjects().size()), Style.PLAIN);
        if (shown.running()) {
            painter.field(2, row++, width, "Devices", String.valueOf(shown.devices()), Style.PLAIN);
            painter.field(2, row++, width, "Requests", String.valueOf(shown.requests()), shown.requests() > 0 ? Style.WARNING : Style.PLAIN);
        }
        row++;
        for (Hint action : hints()) {
            if (row < top + height - 1) {
                painter.text(2, row, action.key(), 3, Style.KEY);
                painter.text(5, row++, action.label(), width - 3, Style.PLAIN);
            }
        }

        int right = painter.columns() - half;
        painter.pane(half, top, right, height, "Configuration", false);
        int column = half + 2;
        int inner = right - 4;
        row = top + 1;
        painter.field(column, row++, inner, "File", Painter.folder(config.path()), Style.PLAIN);
        painter.field(column, row++, inner, "Language", read.language().orElse("system"), Style.PLAIN);
        painter.field(column, row++, inner, "Port", String.valueOf(read.port()), Style.PLAIN);
        painter.field(column, row++, inner, "Network", read.network() ? "on" : "off", Style.PLAIN);
        painter.field(column, row++, inner, "Network port", String.valueOf(read.networkPort()), Style.PLAIN);
        painter.field(column, row++, inner, "Guided mode", read.freeNavigation() ? "off" : "on", Style.PLAIN);
        painter.field(column, row++, inner, "Tracks folder", read.tracksDir().map(Painter::folder).orElse("—"), Style.PLAIN);
        if (shown.running() && shown.network() != null && read.network() != shown.network().path("enabled").asBoolean()) {
            painter.text(column, ++row, "Restart to apply.", inner, Style.WARNING);
        }
    }

    /** The network addresses of a running server, else what the configuration asks. */
    private static List<String> network(Status shown, BoussoleConfig read) {
        List<String> lines = new ArrayList<>();
        if (shown.running() && shown.network() != null) {
            JsonNode network = shown.network();
            if (network.path("enabled").asBoolean()) {
                network.path("hosts").forEach(host -> lines.add("https://" + host.asText() + ":" + network.path("port").asInt() + "/"));
            }
        } else if (read.network()) {
            lines.add("on, port " + read.networkPort());
        }
        if (lines.isEmpty()) {
            lines.add("off");
        }
        return lines;
    }

    @Override
    public boolean key(KeyStroke key) {
        if (key.getKeyType() != KeyType.Character) {
            return false;
        }
        switch (key.getCharacter()) {
            case 'o' -> app.job("Opening…", () -> {
                control.start(() -> { });
                return (control.open("") ? "Opened " : "Open ") + control.url("");
            });
            case 's' -> {
                if (running()) {
                    app.job("Stopping…", () -> control.stop() ? "Stopped." : "Not running.");
                } else {
                    app.job("Starting…", () -> {
                        control.start(() -> { });
                        return "Started.";
                    });
                }
            }
            case 'R' -> app.job("Restarting…", () -> {
                control.stop();
                control.start(() -> { });
                return "Restarted.";
            });
            case 'w' -> {
                boolean on = !config.read().network();
                config.setNetwork(on);
                String done = "Network " + (on ? "on." : "off.");
                if (running()) {
                    app.job("Restarting…", () -> {
                        control.stop();
                        control.start(() -> { });
                        return done;
                    });
                } else {
                    app.say(done);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Hint> hints() {
        return List.of(new Hint("o", "Open"), new Hint("s", running() ? "Stop" : "Start"), new Hint("R", "Restart"),
                new Hint("w", config.read().network() ? "Network off" : "Network on"));
    }
}
