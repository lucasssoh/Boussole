package io.github.lucasssoh.boussole.cli.admin;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import io.github.lucasssoh.boussole.cli.admin.Painter.Style;
import io.github.lucasssoh.boussole.cli.devices.DeviceCommandException;
import io.github.lucasssoh.boussole.cli.devices.Devices;
import io.github.lucasssoh.boussole.cli.devices.LocalServer;
import io.nayuki.qrcodegen.QrCode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;

/** F2: the requests waiting, the paired devices, and a code with its QR code to pair one more. */
final class DevicesTab implements AdminTab {

    private static final List<Hint> LIST = List.of(new Hint("a", "Pair"), new Hint("Enter", "Accept"), new Hint("Del", "Deny"),
            new Hint("n", "Rename"), new Hint("c", "Access"), new Hint("r", "Remove"));
    private static final List<Hint> PAIRING = List.of(new Hint("Tab", "Access"), new Hint("Esc", "Close"));

    private static final DateTimeFormatter SEEN_TODAY = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter SEEN_BEFORE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AdminApp app;
    private final BoussoleConfigFile config;
    private volatile JsonNode all;
    private volatile String problem = "";
    private volatile Pairing pairing;
    private int selected;

    DevicesTab(AdminApp app, BoussoleConfigFile config) {
        this.app = app;
        this.config = config;
    }

    /** A code on offer, and the devices paired before it, to see which one used it. */
    private record Pairing(JsonNode offer, Set<String> before) {

        Instant expiresAt() {
            return Instant.parse(offer.path("expiresAt").asText());
        }

        boolean readOnly() {
            return offer.path("access").asText().equals("read-only");
        }
    }

    /** A line of the lists: a request, or a paired device. */
    private record Item(boolean request, JsonNode node) {

        String name() {
            return node.path("name").asText();
        }
    }

    @Override
    public String name() {
        return "Devices";
    }

    private LocalServer server() {
        return LocalServer.on(config.read().port());
    }

    @Override
    public void refresh() {
        if (!app.running()) {
            all = null;
            problem = "Server stopped.";
            pairing = null;
            return;
        }
        try {
            JsonNode read = server().get("/api/devices");
            all = read;
            problem = "";
            Pairing open = pairing;
            if (open != null) {
                for (JsonNode device : Devices.list(read)) {
                    if (!open.before().contains(device.path("id").asText())) {
                        pairing = null;
                        app.say("Paired  " + device.path("name").asText() + "  " + device.path("address").asText() + "  "
                                + device.path("access").asText());
                        return;
                    }
                }
                if (Instant.now().isAfter(open.expiresAt())) {
                    pairing = null;
                    app.say("Code expired.");
                }
            }
        } catch (DeviceCommandException e) {
            all = null;
            problem = e.getMessage();
        }
    }

    private List<Item> items() {
        JsonNode read = all;
        List<Item> items = new ArrayList<>();
        if (read != null) {
            read.path("requests").forEach(request -> items.add(new Item(true, request)));
            read.path("devices").forEach(device -> items.add(new Item(false, device)));
        }
        return items;
    }

    @Override
    public void draw(Painter painter, int top, int height) {
        int columns = painter.columns();
        Pairing open = pairing;
        if (open != null) {
            drawPairing(painter, top, height, open);
            return;
        }
        JsonNode read = all;
        if (read == null) {
            painter.pane(0, top, columns, height, "Devices", false);
            painter.text(2, top + 1, problem, columns - 4, Style.DIM);
            return;
        }
        List<Item> items = items();
        selected = Math.max(0, Math.min(selected, items.size() - 1));
        int requests = read.path("requests").size();
        int requestsHeight = Math.max(1, requests) + 3;
        int width = columns - 4;

        painter.pane(0, top, columns, requestsHeight, "Requests", requests > 0);
        int name = Math.min(28, width / 3);
        painter.text(2, top + 1, "  " + Painter.column("Name", name) + "  " + Painter.column("Address", 16) + "  " + Painter.column("Code", 9)
                + "  Expires", width, Style.DIM);
        if (requests == 0) {
            painter.text(4, top + 2, "none", width - 2, Style.DIM);
        }
        int row = top + 2;
        for (int index = 0; index < requests; index++) {
            JsonNode request = read.path("requests").path(index);
            boolean chosen = index == selected;
            String line = (chosen ? "▸ " : "  ") + Painter.column(request.path("name").asText(), name) + "  "
                    + Painter.column(request.path("address").asText(), 16) + "  ";
            int end = painter.text(2, row, line, width, chosen ? Style.SELECTED : Style.PLAIN);
            end = painter.text(end, row, Painter.column(Devices.code(request.path("code").asText()), 9), width - (end - 2), Style.CODE);
            painter.text(end, row, "  " + left(Instant.parse(request.path("expiresAt").asText())), width - (end - 2), chosen ? Style.SELECTED : Style.PLAIN);
            row++;
        }

        int devicesTop = top + requestsHeight;
        painter.pane(0, devicesTop, columns, height - requestsHeight, "Paired devices", false);
        painter.text(2, devicesTop + 1, "  " + Painter.column("Name", name) + "  " + Painter.column("Address", 16) + "  "
                + Painter.column("Access", 10) + "  Seen", width, Style.DIM);
        List<JsonNode> devices = Devices.list(read);
        if (devices.isEmpty()) {
            painter.text(4, devicesTop + 2, "none", width - 2, Style.DIM);
        }
        row = devicesTop + 2;
        int room = height - requestsHeight - 3;
        int first = Math.max(0, selected - requests - room + 1);
        for (int index = first; index < devices.size() && row < devicesTop + 2 + room; index++) {
            JsonNode device = devices.get(index);
            boolean chosen = requests + index == selected;
            String line = (chosen ? "▸ " : "  ") + Painter.column(device.path("name").asText(), name) + "  "
                    + Painter.column(device.path("address").asText(), 16) + "  " + Painter.column(device.path("access").asText(), 10)
                    + "  " + seen(Instant.parse(device.path("lastSeen").asText()));
            painter.text(2, row++, Painter.column(line, width), width, chosen ? Style.SELECTED : Style.PLAIN);
        }
    }

    private void drawPairing(Painter painter, int top, int height, Pairing open) {
        int columns = painter.columns();
        painter.pane(0, top, columns, height, "Pair a device", true);
        JsonNode offer = open.offer();
        String url = offer.path("urls").path(0).asText();
        QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
        int column = 2;
        if ((qr.size + 4 + 1) / 2 <= height - 2) {
            painter.qr(2, top + 1, qr);
            column = 2 + Painter.qrWidth(qr) + 3;
        }
        int width = columns - column - 2;
        int row = top + 2;
        painter.field(column, row++, width, "Address", url.replaceFirst("\\?code=.*", ""), Style.PLAIN);
        for (int index = 1; index < offer.path("urls").size(); index++) {
            painter.field(column, row++, width, "", offer.path("urls").path(index).asText().replaceFirst("\\?code=.*", ""), Style.DIM);
        }
        row++;
        painter.field(column, row++, width, "Code", Devices.code(offer.path("code").asText()), Style.CODE);
        painter.field(column, row++, width, "Expires in", left(open.expiresAt()), Style.PLAIN);
        row++;
        painter.field(column, row++, width, "Access", (open.readOnly() ? "( )" : "(•)") + " Full", Style.PLAIN);
        painter.field(column, row++, width, "", (open.readOnly() ? "(•)" : "( )") + " Read-only", Style.PLAIN);
        row++;
        JsonNode network = all == null ? null : all.path("network");
        if (network != null) {
            painter.field(column, row, width, "Fingerprint", network.path("fingerprint").asText(), Style.PLAIN);
        }
    }

    /** The time left, {@code 1:48}. */
    static String left(Instant expiresAt) {
        long seconds = Math.max(0, Duration.between(Instant.now(), expiresAt).toSeconds());
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    /** {@code 17:02} today, else the day: {@code 2026-10-29}. */
    static String seen(Instant instant) {
        ZonedDateTime seen = instant.atZone(ZoneId.systemDefault());
        return seen.toLocalDate().equals(LocalDate.now()) ? SEEN_TODAY.format(seen) : SEEN_BEFORE.format(seen);
    }

    @Override
    public boolean key(KeyStroke key) {
        Pairing open = pairing;
        if (open != null) {
            if (key.getKeyType() == KeyType.Escape) {
                pairing = null;
                return true;
            }
            if (key.getKeyType() == KeyType.Tab) {
                pair(!open.readOnly());
                return true;
            }
            return false;
        }
        List<Item> items = items();
        Item item = items.isEmpty() ? null : items.get(Math.max(0, Math.min(selected, items.size() - 1)));
        switch (key.getKeyType()) {
            case ArrowUp -> selected = Math.max(0, selected - 1);
            case ArrowDown -> selected = Math.min(Math.max(0, items.size() - 1), selected + 1);
            case Enter -> {
                if (item != null && item.request()) {
                    String code = item.node().path("code").asText();
                    app.job(null, () -> {
                        server().post("/api/devices/requests/" + code + "/accept", Map.of("access", "full"));
                        return "Accepted  " + item.name();
                    });
                }
            }
            case Delete, Backspace -> {
                if (item != null && item.request()) {
                    String code = item.node().path("code").asText();
                    app.job(null, () -> {
                        server().post("/api/devices/requests/" + code + "/deny", Map.of());
                        return "Denied  " + item.name();
                    });
                } else if (item != null) {
                    remove(item);
                }
            }
            case Character -> {
                return character(key.getCharacter(), item);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean character(char typed, Item item) {
        Item device = item != null && !item.request() ? item : null;
        switch (typed) {
            case 'a' -> {
                JsonNode read = all;
                if (read == null) {
                    app.error(problem);
                } else if (!read.path("network").path("enabled").asBoolean()) {
                    app.error("Network off. F1, then w.");
                } else {
                    pair(false);
                }
            }
            case 'r' -> {
                if (device != null) {
                    remove(device);
                }
            }
            case 'n' -> {
                if (device != null) {
                    String id = device.node().path("id").asText();
                    app.ask("Name:", device.name(), name -> {
                        if (!name.isEmpty()) {
                            app.job(null, () -> {
                                server().patch("/api/devices/" + id, Map.of("name", name));
                                return "Renamed  " + name;
                            });
                        }
                    });
                }
            }
            case 'c' -> {
                if (device != null) {
                    String id = device.node().path("id").asText();
                    String access = device.node().path("access").asText().equals("full") ? "read-only" : "full";
                    app.job(null, () -> {
                        server().patch("/api/devices/" + id, Map.of("access", access));
                        return "Access  " + device.name() + "  " + access;
                    });
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void remove(Item device) {
        String id = device.node().path("id").asText();
        app.confirm("Remove " + device.name() + "?", () -> app.job(null, () -> {
            server().delete("/api/devices/" + id);
            return "Removed  " + device.name();
        }));
    }

    /** A new code; the one before stops working, as one code at a time is on offer. */
    private void pair(boolean readOnly) {
        app.job(null, () -> {
            Set<String> before = StreamSupport.stream(server().get("/api/devices").path("devices").spliterator(), false)
                    .map(device -> device.path("id").asText())
                    .collect(Collectors.toSet());
            pairing = new Pairing(server().post("/api/devices/pairing", Map.of("access", Devices.access(readOnly))), before);
            return null;
        });
    }

    @Override
    public List<Hint> hints() {
        return pairing != null ? PAIRING : LIST;
    }
}
