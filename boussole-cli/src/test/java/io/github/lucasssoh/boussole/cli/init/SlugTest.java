package io.github.lucasssoh.boussole.cli.init;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugTest {

    @Test
    void folderNamesBecomeIds() {
        assertThat(Slug.of("L&MC")).isEqualTo("l-mc");
        assertThat(Slug.of("Réseaux")).isEqualTo("reseaux");
        assertThat(Slug.of("A&C")).isEqualTo("a-c");
        assertThat(Slug.of("  Optimisation Combinatoire ")).isEqualTo("optimisation-combinatoire");
        assertThat(Slug.of("&&")).isEqualTo("subject");
    }
}
