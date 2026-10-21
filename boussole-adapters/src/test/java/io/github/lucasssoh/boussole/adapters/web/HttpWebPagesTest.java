package io.github.lucasssoh.boussole.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.lucasssoh.boussole.core.reference.PagePreview;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpWebPagesTest {

    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        page("/open", Map.of(), "<html><head><title>Python &amp; you</title></head></html>");
        page("/options", Map.of("X-Frame-Options", "SAMEORIGIN"), "<title>Closed</title>");
        page("/policy", Map.of("Content-Security-Policy", "default-src 'self'; frame-ancestors 'self'"), "<title>Closed</title>");
        page("/anyone", Map.of("Content-Security-Policy", "frame-ancestors *"), "<title>Open to all</title>");
        server.createContext("/gone", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void page(String path, Map<String, String> headers, String html) {
        server.createContext(path, exchange -> {
            headers.forEach((name, value) -> exchange.getResponseHeaders().add(name, value));
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
    }

    private PagePreview preview(String path) {
        return new HttpWebPages(true).preview(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path));
    }

    @Test
    void a_page_without_frame_rules_shows_beside_the_text_with_its_title() {
        assertThat(preview("/open")).isEqualTo(new PagePreview(true, true, java.util.Optional.of("Python & you")));
    }

    @Test
    void a_page_that_refuses_frames_is_opened_in_a_tab() {
        assertThat(preview("/options").embeddable()).isFalse();
        assertThat(preview("/policy").embeddable()).isFalse();
        assertThat(preview("/anyone").embeddable()).isTrue();
    }

    @Test
    void a_page_that_does_not_answer_is_unreachable() {
        assertThat(preview("/gone")).isEqualTo(PagePreview.UNREACHABLE);
    }

    @Test
    void this_machine_and_its_network_are_never_looked_at() {
        URI local = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/open");

        assertThat(new HttpWebPages().preview(local)).isEqualTo(PagePreview.UNREACHABLE);
    }
}
