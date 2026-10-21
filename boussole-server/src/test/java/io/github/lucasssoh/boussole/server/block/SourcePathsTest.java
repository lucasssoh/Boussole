package io.github.lucasssoh.boussole.server.block;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SourcePathsTest {

    @Test
    void a_document_is_found_from_the_subject_folder() {
        assertThat(SourcePaths.of("sources", "cours/td1.pdf")).isEqualTo("sources/cours/td1.pdf");
        assertThat(SourcePaths.of(".", "td1.pdf")).isEqualTo("td1.pdf");
    }

    @Test
    void a_sources_folder_outside_the_subject_is_named_by_its_mark_never_by_dots() {
        assertThat(SourcePaths.of("../python/sources", "cours/td1.pdf")).isEqualTo("@sources/cours/td1.pdf");
    }
}
