package io.github.lucasssoh.boussole.format.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChapterLinksTest {

    private static ChapterLinks links(Map<String, Set<String>> after) {
        return new ChapterLinks(new LinkedHashMap<>(after));
    }

    @Test
    void aLinkAnotherPathGivesIsNotDrawn() {
        ChapterLinks links = links(Map.of("a", Set.of(), "b", Set.of("a"), "c", Set.of("a", "b")));

        assertThat(links.direct()).containsEntry("c", Set.of("b")).containsEntry("b", Set.of("a"));
    }

    @Test
    void chaptersInOneOrderHaveNoLoop() {
        assertThat(links(Map.of("a", Set.of(), "b", Set.of("a"))).loop()).isEmpty();
    }

    @Test
    void aLoopIsFound() {
        assertThat(links(Map.of("a", Set.of("b"), "b", Set.of("a"))).loop()).isPresent();
    }
}
