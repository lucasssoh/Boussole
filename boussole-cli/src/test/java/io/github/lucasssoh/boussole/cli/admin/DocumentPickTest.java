package io.github.lucasssoh.boussole.cli.admin;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.format.source.SourceInventory.SourceState;
import io.github.lucasssoh.boussole.format.source.SourceInventory.State;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DocumentPickTest {

    private final DocumentPick pick = new DocumentPick(Path.of("algo"), List.of(document("cours7.pdf"), document("td7.pdf"),
            document("cours8.pdf")));

    @Test
    void nothingIsTickedAtFirst() {
        assertThat(pick.chosen()).isEmpty();
    }

    @Test
    void onlyTheTickedDocumentsAreChosenInTheOrderOfTheList() {
        pick.move(2);
        pick.toggle();
        pick.move(-2);
        pick.toggle();

        assertThat(pick.chosen()).containsExactly("cours7.pdf", "cours8.pdf");

        pick.toggle();
        assertThat(pick.chosen()).containsExactly("cours8.pdf");
    }

    @Test
    void theCursorStaysOnTheList() {
        pick.move(-1);
        assertThat(pick.cursor()).isZero();
        pick.move(10);
        assertThat(pick.cursor()).isEqualTo(2);
    }

    @Test
    void allTicksEveryDocumentThenNone() {
        pick.toggle();
        pick.toggleAll();
        assertThat(pick.chosen()).hasSize(3);
        pick.toggleAll();
        assertThat(pick.chosen()).isEmpty();
    }

    private static SourceState document(String file) {
        return new SourceState(file, State.NEW, Optional.of("00"), Optional.empty());
    }
}
