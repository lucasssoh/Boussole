package io.github.lucasssoh.boussole.cli.devices;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.cli.BoussoleCommand;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class DevicesCommandTest {

    @Test
    void the_qr_code_is_drawn_two_rows_a_line_with_its_margin() {
        String[] lines = QrText.of("https://192.168.1.14:7421/pair?code=382615").split("\n");

        // Version 3 is 29 modules, with a margin of 2 on each side: 33 rows, 17 lines.
        assertThat(lines).hasSize(17);
        assertThat(lines[0]).contains("▀").endsWith("\u001b[0m");
    }

    @Test
    void codes_read_in_two_groups() {
        assertThat(Devices.code("382615")).isEqualTo("382 615");
        assertThat(Devices.access(true)).isEqualTo("read-only");
    }

    @Test
    void devices_offers_every_action() {
        CommandLine devices = new CommandLine(new BoussoleCommand()).getSubcommands().get("devices");

        assertThat(devices.getSubcommands()).containsOnlyKeys("pair", "accept", "deny", "remove", "rename", "access");
    }
}
