package io.github.lucasssoh.boussole.core.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ShowDocumentTest {

    private final List<Path> converted = new ArrayList<>();
    private final Path pdf = Path.of("/cache/abc.pdf");

    @Test
    void a_pdf_or_an_image_is_shown_as_it_is() {
        ShowDocument show = new ShowDocument(document -> fail());

        assertThat(show.fileToShow(Path.of("cours/slides.pdf"))).contains(Path.of("cours/slides.pdf"));
        assertThat(show.fileToShow(Path.of("cours/schema.PNG"))).contains(Path.of("cours/schema.PNG"));
        assertThat(show.fileToShow(Path.of("cours/README"))).contains(Path.of("cours/README"));
    }

    @Test
    void a_word_or_powerpoint_document_is_shown_as_its_pdf() {
        ShowDocument show = new ShowDocument(document -> {
            converted.add(document);
            return Optional.of(pdf);
        });

        for (String name : List.of("td.doc", "td.DOCX", "td.odt", "cours.ppt", "cours.pptx", "cours.odp")) {
            assertThat(show.fileToShow(Path.of("Michel", name))).contains(pdf);
        }
        assertThat(converted).hasSize(6);
    }

    @Test
    void an_office_document_without_a_pdf_has_nothing_to_show() {
        ShowDocument show = new ShowDocument(document -> Optional.empty());

        assertThat(show.fileToShow(Path.of("Michel/DP_composite_exo.doc"))).isEmpty();
    }

    private Optional<Path> fail() {
        throw new AssertionError("only office documents are converted");
    }
}
