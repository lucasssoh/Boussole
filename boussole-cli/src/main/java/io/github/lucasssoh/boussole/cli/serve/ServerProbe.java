package io.github.lucasssoh.boussole.cli.serve;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Finds out whether a Boussole server answers on a port of this machine, and talks to it. */
final class ServerProbe {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final int port;

    ServerProbe(int port) {
        this.port = port;
    }

    String url(String path) {
        return "http://localhost:" + port + "/" + path;
    }

    /** Whether something listens on the port at all. */
    boolean portTaken() {
        try (var socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("127.0.0.1", port), (int) TIMEOUT.toMillis());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    boolean isBoussole() {
        try {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create(url("api/server"))).timeout(TIMEOUT).build(),
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().contains("\"name\":\"boussole\"");
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    boolean waitUntilUp(Duration limit) throws InterruptedException {
        long deadline = System.nanoTime() + limit.toNanos();
        while (System.nanoTime() < deadline) {
            if (isBoussole()) {
                return true;
            }
            Thread.sleep(250);
        }
        return false;
    }

    void stop() throws IOException, InterruptedException {
        client.send(HttpRequest.newBuilder(URI.create(url("api/server/stop")))
                .header("X-Boussole", "1")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(TIMEOUT)
                .build(), HttpResponse.BodyHandlers.discarding());
    }
}
