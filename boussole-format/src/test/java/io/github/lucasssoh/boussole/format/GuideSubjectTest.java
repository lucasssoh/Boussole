package io.github.lucasssoh.boussole.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.version.TrackVersions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The tracks shipped with Boussole ({@code guide/<track>/<language>}) are subjects like any other:
 * each stays valid, and its languages are aligned versions of one track (TrackVersions).
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
    void eachLanguageIsValidAndBothVersionsAreAligned(Path track) {
        Course english = course(track.resolve("en"));
        Course french = course(track.resolve("fr"));

        assertThat(english.sessions()).isNotEmpty();
        assertThat(TrackVersions.compare(english, french)).as("fr against en").extracting(Diagnostic::message).isEmpty();
        assertThat(TrackVersions.compare(french, english)).as("en against fr").extracting(Diagnostic::message).isEmpty();
    }

    private static Course course(Path folder) {
        InspectionReport report = new SubjectInspector().inspect(folder);
        assertThat(report.diagnostics()).as(folder.toString()).isEmpty();
        return report.course().orElseThrow();
    }
}
