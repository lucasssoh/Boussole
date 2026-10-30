package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.lucasssoh.boussole.adapters.config.BoussoleConfigFile;
import java.io.IOException;
import tools.jackson.core.JacksonException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/** The running Boussole of this computer, asked through its API on 127.0.0.1. */
final class LocalServer {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final int port;

    private LocalServer(int port) {
        this.port = port;
    }

    static LocalServer ofConfig() {
        return new LocalServer(BoussoleConfigFile.forCurrentUser().read().port());
    }

    JsonNode get(String path) {
        return send("GET", path, null);
    }

    JsonNode post(String path, Map<String, String> body) {
        return send("POST", path, body);
    }

    JsonNode patch(String path, Map<String, String> body) {
        return send("PATCH", path, body);
    }

    void delete(String path) {
        send("DELETE", path, null);
    }

    /** The body as JSON; a page of error in HTML reads as nothing. */
    private static JsonNode parsed(String body) {
        try {
            return body.isBlank() ? JSON.createObjectNode() : JSON.readTree(body);
        } catch (JacksonException e) {
            return JSON.createObjectNode();
        }
    }

    private JsonNode send(String method, String path, Map<String, String> body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(TIMEOUT)
                    .header("X-Boussole", "1")
                    .header("Content-Type", "application/json");
            request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode json = parsed(response.body());
            if (response.statusCode() == 404 && path.startsWith("/api/devices")) {
                throw new DeviceCommandException("The running Boussole is older than this command. Restart it: boussole stop, then boussole open");
            }
            if (response.statusCode() >= 400) {
                throw new DeviceCommandException(json.path("message").asText("HTTP " + response.statusCode()));
            }
            return json;
        } catch (IOException e) {
            throw new DeviceCommandException("Boussole is not running on port " + port + ". Start it with: boussole open");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeviceCommandException("interrupted");
        }
    }
}
