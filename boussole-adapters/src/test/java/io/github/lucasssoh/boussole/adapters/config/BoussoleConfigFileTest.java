package io.github.lucasssoh.boussole.adapters.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoussoleConfigFileTest {

    @TempDir
    Path home;

    private BoussoleConfigFile config() {
        return new BoussoleConfigFile(home.resolve(".config/boussole/config.yaml"), home);
    }

    @Test
    void without_a_file_the_defaults_apply() {
        assertThat(config().read()).isEqualTo(BoussoleConfig.defaults());
    }

    @Test
    void reads_the_language_the_port_and_the_folders_with_a_tilde() throws Exception {
        Files.createDirectories(home.resolve(".config/boussole"));
        Files.writeString(config().path(), "language: fr\nport: 7500\nfree_navigation: true\nsubjects:\n  - ~/courses/OC\n  - /srv/RESEAUX\n");

        BoussoleConfig read = config().read();

        assertThat(read.language()).contains("fr");
        assertThat(read.port()).isEqualTo(7500);
        assertThat(read.freeNavigation()).isTrue();
        assertThat(read.subjects()).containsExactly(home.resolve("courses/OC"), Path.of("/srv/RESEAUX"));
    }

    @Test
    void a_key_written_twice_is_said_with_its_line() throws Exception {
        Files.createDirectories(home.resolve(".config/boussole"));
        Files.writeString(config().path(), "subjects:\n  - ~/courses/OC\nnetwork: true\nnetwork: true\n");

        assertThatThrownBy(() -> config().read())
                .isInstanceOf(InvalidConfigException.class)
                .hasMessage(config().path() + ", line 4: found duplicate key network");
    }

    @Test
    void other_devices_reach_boussole_only_when_the_network_is_on() throws Exception {
        assertThat(config().read().network()).isFalse();
        Files.createDirectories(home.resolve(".config/boussole"));
        Files.writeString(config().path(), "network: true\nnetwork_port: 7600\n");

        assertThat(config().read().network()).isTrue();
        assertThat(config().read().networkPort()).isEqualTo(7600);
    }

    @Test
    void adds_a_folder_once_and_keeps_the_rest_of_the_file() throws Exception {
        Files.createDirectories(home.resolve(".config/boussole"));
        Files.writeString(config().path(), "# my settings\nsubjects:\n  - ~/courses/OC\n\nport: 7500 # not the default\n");

        assertThat(config().addSubject(home.resolve("courses/RESEAUX"))).isTrue();
        assertThat(config().addSubject(home.resolve("courses/RESEAUX"))).isFalse();

        assertThat(Files.readString(config().path())).isEqualTo(
                "# my settings\nsubjects:\n  - ~/courses/OC\n  - ~/courses/RESEAUX\n\nport: 7500 # not the default\n");
    }

    @Test
    void creates_the_file_with_the_first_folder() {
        config().addSubject(home.resolve("courses/OC"));

        assertThat(config().read().subjects()).containsExactly(home.resolve("courses/OC"));
    }

    @Test
    void fills_an_empty_list() throws Exception {
        Files.createDirectories(home.resolve(".config/boussole"));
        Files.writeString(config().path(), "subjects: []\n");

        config().addSubject(home.resolve("courses/OC"));

        assertThat(config().read().subjects()).containsExactly(home.resolve("courses/OC"));
    }
}
