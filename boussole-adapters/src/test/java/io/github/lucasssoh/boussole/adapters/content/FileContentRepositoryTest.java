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

        assertThat(content.versions("reseaux")).singleElement().satisfies(course -> assertThat(course.sessions()).hasSize(2));
        assertThat(content.versions("oc")).isEmpty();
    }

    @Test
    void the_versions_of_a_track_share_its_id_and_a_second_folder_in_the_same_language_is_left_out() {
        Path guide = Path.of("../guide/getting-started");
        FileContentRepository content = new FileContentRepository(() -> List.of(guide.resolve("en"), guide.resolve("fr"), guide.resolve("fr")));

        assertThat(content.versions("boussole-guide")).extracting(course -> course.subject().lang()).containsExactly("en", "fr");
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
        return content.versions("reseaux").getFirst().sessions().getFirst().session().parts().toString();
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
