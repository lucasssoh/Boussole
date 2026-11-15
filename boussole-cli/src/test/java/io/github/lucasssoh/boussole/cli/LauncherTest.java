package io.github.lucasssoh.boussole.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class LauncherTest {

    @Test
    void theJavaOptionsComeBeforeTheJar() {
        List<String> command = Launcher.commandLine(List.of("-Xmx256m"), Path.of("server.jar"), "serve");

        assertThat(command.subList(1, command.size())).containsExactly("-Xmx256m", "-jar", "server.jar", "serve");
    }

    @Test
    void aPackagedBoussoleRunsItsServerWithTheSameOptions() throws IOException {
        String javaOptions = Files.readAllLines(Path.of("../bin/package")).stream()
                .filter(line -> line.contains("--java-options"))
                .findFirst().orElseThrow();

        assertThat(javaOptions).contains(Launcher.SERVER_JAVA_OPTIONS);
    }
}
