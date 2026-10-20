package io.github.lucasssoh.boussole.adapters.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileContentRepositoryTest {

    @TempDir
    Path folder;

    @Test
    void reads_the_example_subject() {
        FileContentRepository content = new FileContentRepository(() -> List.of(Path.of("../examples/reseaux")));

        assertThat(content.course("reseaux")).hasValueSatisfying(course -> assertThat(course.sessions()).hasSize(2));
        assertThat(content.course("oc")).isEmpty();
    }

    @Test
    void skips_a_folder_that_is_not_a_subject() {
        assertThat(new FileContentRepository(() -> List.of(folder)).courses()).isEmpty();
    }

    @Test
    void reads_a_session_again_once_its_file_changed() throws IOException {
        copy(Path.of("../examples/reseaux"), folder);
        FileContentRepository content = new FileContentRepository(() -> List.of(folder));
        Path session = folder.resolve("cursus/01-couche-reseau/11-role-couche-reseau.md");
        assertThat(firstSessionText(content)).contains("Routage ou forwarding");

        Files.writeString(session, Files.readString(session).replace("Routage ou forwarding", "Forwarding ou routage"));
        Files.setLastModifiedTime(session, FileTime.from(Instant.now().plusSeconds(5)));

        assertThat(firstSessionText(content)).contains("Forwarding ou routage");
    }

    private static String firstSessionText(FileContentRepository content) {
        return content.course("reseaux").orElseThrow().sessions().getFirst().session().parts().toString();
    }

    private static void copy(Path from, Path to) throws IOException {
        try (Stream<Path> files = Files.walk(from)) {
            for (Path source : files.toList()) {
                Path target = to.resolve(from.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target);
                }
            }
        }
    }
}
