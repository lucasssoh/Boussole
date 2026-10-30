package io.github.lucasssoh.boussole.server;

import java.util.Optional;
import org.springframework.context.ConfigurableApplicationContext;

/** A started server, the port it listens on, and how other devices reach it, when they may. */
public record RunningServer(ConfigurableApplicationContext context, int port, Optional<NetworkSettings> network) implements AutoCloseable {

    public String url() {
        return "http://localhost:" + port + "/";
    }

    /** The first address of the computer, over HTTPS. */
    public Optional<String> networkUrl() {
        return network.filter(found -> !found.hosts().isEmpty()).map(found -> "https://" + found.hosts().getFirst() + ":" + found.port() + "/");
    }

    @Override
    public void close() {
        context.close();
    }
}
