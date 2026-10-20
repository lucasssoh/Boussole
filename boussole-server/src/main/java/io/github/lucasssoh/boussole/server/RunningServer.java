package io.github.lucasssoh.boussole.server;

import org.springframework.context.ConfigurableApplicationContext;

/** A started server and the port it listens on. */
public record RunningServer(ConfigurableApplicationContext context, int port) implements AutoCloseable {

    public String url() {
        return "http://localhost:" + port + "/";
    }

    @Override
    public void close() {
        context.close();
    }
}
