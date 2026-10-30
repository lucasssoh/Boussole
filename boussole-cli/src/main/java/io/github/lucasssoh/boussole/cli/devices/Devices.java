package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/** What the {@code boussole devices} commands share: finding a device, writing times and codes. */
final class Devices {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private Devices() {
    }

    static List<JsonNode> list(JsonNode devices) {
        return StreamSupport.stream(devices.path("devices").spliterator(), false).toList();
    }

    /** A device by its ID or its name; a name two devices share is refused. */
    static JsonNode find(LocalServer server, String reference) {
        List<JsonNode> matching = new ArrayList<>();
        for (JsonNode device : list(server.get("/api/devices"))) {
            if (device.path("id").asText().equals(reference)) {
                return device;
            }
            if (device.path("name").asText().equalsIgnoreCase(reference)) {
                matching.add(device);
            }
        }
        if (matching.size() == 1) {
            return matching.getFirst();
        }
        throw new DeviceCommandException(matching.isEmpty()
                ? "No paired device " + reference + ". See: boussole devices"
                : "Several devices are named " + reference + ": give its ID. See: boussole devices");
    }

    static String time(JsonNode instant) {
        return TIME.format(Instant.parse(instant.asText()));
    }

    /** {@code 382615} as {@code 382 615}. */
    static String code(String code) {
        return code.length() == 6 ? code.substring(0, 3) + " " + code.substring(3) : code;
    }

    static String access(boolean readOnly) {
        return readOnly ? "read-only" : "full";
    }
}
