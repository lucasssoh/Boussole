package io.github.lucasssoh.boussole.format.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** The terms of a subject. */
public record CourseLexicon(List<LexiconTerm> terms) {

    public static CourseLexicon empty() {
        return new CourseLexicon(List.of());
    }

    public Optional<LexiconTerm> byId(String id) {
        return terms.stream().filter(term -> term.id().equals(id)).findFirst();
    }

    /** The term whose name or one of its aliases is {@code text}, ignoring case. */
    public Optional<LexiconTerm> byName(String text) {
        String wanted = normalise(text);
        return terms.stream()
                .filter(term -> normalise(term.term()).equals(wanted)
                        || term.aliases().stream().map(CourseLexicon::normalise).anyMatch(wanted::equals))
                .findFirst();
    }

    private static String normalise(String text) {
        return text.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
