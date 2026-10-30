package io.github.lucasssoh.boussole.server;

import java.nio.file.Path;
import java.util.List;

/**
 * How other devices of the local network reach the server, when the configuration lets them: over
 * HTTPS on {@code port}, with the certificate in {@code keystore}, by one of {@code hosts} (the
 * computer's addresses and name; a request naming another host is refused).
 */
public record NetworkSettings(int port, Path keystore, String password, List<String> hosts, String fingerprint) {

    public NetworkSettings {
        hosts = List.copyOf(hosts);
    }
}
