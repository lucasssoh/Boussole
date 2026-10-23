package io.github.lucasssoh.boussole.cli.serve;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerProbeTest {

    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        answer("/api/subjects/oc", "{\"id\":\"oc\",\"name\":\"OC\",\"resume\":{\"id\":\"s36\",\"title\":\"Exclusion\"}}");
        answer("/api/subjects/done", "{\"id\":\"done\",\"resume\":null}");
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void answer(String path, String json) {
        server.createContext(path, exchange -> {
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
    }

    @Test
    void the_session_to_resume_comes_from_the_course() {
        ServerProbe probe = new ServerProbe(server.getAddress().getPort());

        assertThat(probe.sessionToResume("oc")).contains("s36");
        assertThat(probe.sessionToResume("done")).isEmpty();
        assertThat(probe.sessionToResume("unknown")).isEmpty();
    }
}
