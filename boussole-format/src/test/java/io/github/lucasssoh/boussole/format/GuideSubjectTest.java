package io.github.lucasssoh.boussole.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The tracks shipped with Boussole ({@code guide/<track>/<language>}) are subjects like any other:
 * each stays valid, and its languages hold the same sessions.
 */
class GuideSubjectTest {

    private static final Path GUIDE = Path.of("..", "guide");

    static Stream<Path> tracks() throws IOException {
        try (Stream<Path> folders = Files.list(GUIDE)) {
            return folders.filter(Files::isDirectory).sorted().toList().stream();
        }
    }

    @ParameterizedTest
    @MethodSource("tracks")
    void eachLanguageIsValidAndHoldsTheSameSessions(Path track) {
        List<String> english = sessions(track.resolve("en"));
        List<String> french = sessions(track.resolve("fr"));

        assertThat(english).isNotEmpty().isEqualTo(french);
    }

    private static List<String> sessions(Path folder) {
        InspectionReport report = new SubjectInspector().inspect(folder);
        assertThat(report.diagnostics()).as(folder.toString()).isEmpty();
        return report.course().orElseThrow().sessions().stream().map(session -> session.entry().id()).toList();
    }
}
