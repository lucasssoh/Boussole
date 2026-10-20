package io.github.lucasssoh.boussole.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import io.github.lucasssoh.boussole.format.model.block.McqChoice;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.model.part.CheckpointPart;
import io.github.lucasssoh.boussole.format.model.part.NotionPart;
import io.github.lucasssoh.boussole.format.model.part.PracticePart;
import io.github.lucasssoh.boussole.format.model.part.WarmupPart;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** The example subject of the repository is the reference: it must stay valid, and read as intended. */
class ExampleSubjectTest {

    private static final Path EXAMPLE = Path.of("..", "examples", "reseaux");

    private final InspectionReport report = new SubjectInspector().inspect(EXAMPLE);

    @Test
    void hasNoDiagnostic() {
        assertThat(report.diagnostics()).isEmpty();
    }

    @Test
    void readsTheSessionInItsParts() {
        CourseSession session = course().sessions().getFirst();

        assertThat(session.id()).hasToString("reseaux.couche-reseau.s11");
        assertThat(session.session().parts())
                .extracting(part -> part.getClass().getSimpleName())
                .containsExactly("WarmupPart", "NotionPart", "NotionPart", "CheckpointPart", "PracticePart");
        assertThat(session.session().parts().get(1)).isInstanceOfSatisfying(NotionPart.class, notion -> {
            assertThat(notion.id()).isEqualTo("couche3");
            assertThat(notion.title()).isEqualTo("Ce que la couche 3 apporte");
        });
        assertThat(session.session().parts().getFirst()).isInstanceOf(WarmupPart.class);
        assertThat(session.session().parts().get(3)).isInstanceOf(CheckpointPart.class);
        assertThat(session.session().parts().get(4)).isInstanceOf(PracticePart.class);
    }

    @Test
    void readsTheAnswers() {
        ExerciseBlock macBits = exercise("s11-a1");
        assertThat(expected(macBits)).isInstanceOfSatisfying(NumberExpectation.class, number -> {
            assertThat(number.values()).containsExactly(Rational.parse("48").orElseThrow());
            assertThat(number.unit()).contains("bits");
        });
        assertThat(macBits.hints()).hasSize(1);
        assertThat(macBits.solution()).isPresent();

        assertThat(expected(exercise("s11-w1"))).isInstanceOfSatisfying(TextExpectation.class,
                text -> assertThat(text.accepted()).hasSize(4));
    }

    @Test
    void readsTheChoicesAndTheirFeedback() {
        McqBlock mcq = course().blocks()
                .filter(McqBlock.class::isInstance).map(McqBlock.class::cast)
                .filter(found -> found.id().equals("s11-a2")).findFirst().orElseThrow();

        assertThat(mcq.choices()).extracting(McqChoice::text).containsExactly("Datagramme", "Circuit virtuel", "Les deux");
        assertThat(mcq.choices()).extracting(McqChoice::correct).containsExactly(false, true, false);
        assertThat(mcq.choices().getFirst().feedback()).get().asString().startsWith("Non : en datagramme");
        assertThat(mcq.choices().get(1).feedback()).isEmpty();
    }

    private Course course() {
        return report.course().orElseThrow();
    }

    private static Object expected(ExerciseBlock exercise) {
        return ((SingleAnswer) exercise.answer().orElseThrow()).expected();
    }

    private ExerciseBlock exercise(String id) {
        return course().blocks()
                .filter(ExerciseBlock.class::isInstance).map(ExerciseBlock.class::cast)
                .filter(exercise -> exercise.id().equals(id)).findFirst().orElseThrow();
    }
}
