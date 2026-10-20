package io.github.lucasssoh.boussole.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** The guide shipped with Boussole is a subject like any other: in each language, it stays valid. */
class GuideSubjectTest {

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr"})
    void hasNoDiagnostic(String language) {
        InspectionReport report = new SubjectInspector().inspect(Path.of("..", "guide", language));

        assertThat(report.diagnostics()).isEmpty();
        assertThat(report.course()).hasValueSatisfying(course -> assertThat(course.sessions()).hasSize(5));
    }
}
