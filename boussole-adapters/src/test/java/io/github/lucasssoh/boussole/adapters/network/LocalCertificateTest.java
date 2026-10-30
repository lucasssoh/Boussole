package io.github.lucasssoh.boussole.adapters.network;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalCertificateTest {

    @TempDir
    Path folder;

    @Test
    void the_certificate_is_kept_while_it_names_every_address() {
        LocalCertificate first = LocalCertificate.ensure(folder, List.of("192.168.1.14", "fedora.local"));
        LocalCertificate again = LocalCertificate.ensure(folder, List.of("192.168.1.14"));

        assertThat(again.fingerprint()).isEqualTo(first.fingerprint());
        assertThat(first.shortFingerprint()).matches("[0-9A-F]{2}( [0-9A-F]{2}){3}");
    }

    @Test
    void a_new_address_makes_a_new_certificate() {
        LocalCertificate first = LocalCertificate.ensure(folder, List.of("192.168.1.14"));
        LocalCertificate moved = LocalCertificate.ensure(folder, List.of("10.0.0.5"));

        assertThat(moved.fingerprint()).isNotEqualTo(first.fingerprint());
    }
}
