package io.github.lucasssoh.boussole.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.LoadedSession;
import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;
import io.github.lucasssoh.boussole.format.model.expectation.NumberExpectation;
import io.github.lucasssoh.boussole.format.model.expectation.Rational;
import io.github.lucasssoh.boussole.format.model.expectation.TextExpectation;
import io.github.lucasssoh.boussole.format.model.block.Choice;
import io.github.lucasssoh.boussole.format.model.block.Exercise;
import io.github.lucasssoh.boussole.format.model.block.Qcm;
import io.github.lucasssoh.boussole.format.model.part.Checkpoint;
import io.github.lucasssoh.boussole.format.model.part.Notion;
import io.github.lucasssoh.boussole.format.model.part.Practice;
import io.github.lucasssoh.boussole.format.model.part.Warmup;
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
        LoadedSession session = course().sessions().getFirst();

        assertThat(session.id()).hasToString("reseaux.couche-reseau.s11");
        assertThat(session.session().parts())
                .extracting(part -> part.getClass().getSimpleName())
                .containsExactly("Warmup", "Notion", "Notion", "Checkpoint", "Practice");
        assertThat(session.session().parts().get(1)).isInstanceOfSatisfying(Notion.class, notion -> {
            assertThat(notion.id()).isEqualTo("couche3");
            assertThat(notion.title()).isEqualTo("Ce que la couche 3 apporte");
        });
        assertThat(session.session().parts().getFirst()).isInstanceOf(Warmup.class);
        assertThat(session.session().parts().get(3)).isInstanceOf(Checkpoint.class);
        assertThat(session.session().parts().get(4)).isInstanceOf(Practice.class);
    }

    @Test
    void readsTheAnswers() {
        Exercise macBits = exercise("s11-a1");
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
        Qcm qcm = course().blocks()
                .filter(Qcm.class::isInstance).map(Qcm.class::cast)
                .filter(found -> found.id().equals("s11-a2")).findFirst().orElseThrow();

        assertThat(qcm.choices()).extracting(Choice::text).containsExactly("Datagramme", "Circuit virtuel", "Les deux");
        assertThat(qcm.choices()).extracting(Choice::correct).containsExactly(false, true, false);
        assertThat(qcm.choices().getFirst().feedback()).get().asString().startsWith("Non : en datagramme");
        assertThat(qcm.choices().get(1).feedback()).isEmpty();
    }

    private Course course() {
        return report.course().orElseThrow();
    }

    private static Object expected(Exercise exercise) {
        return ((SingleAnswer) exercise.answer().orElseThrow()).expected();
    }

    private Exercise exercise(String id) {
        return course().blocks()
                .filter(Exercise.class::isInstance).map(Exercise.class::cast)
                .filter(exercise -> exercise.id().equals(id)).findFirst().orElseThrow();
    }
}
