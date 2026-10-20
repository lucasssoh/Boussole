package io.github.lucasssoh.boussole.cli.init;

import java.text.Normalizer;
import java.util.Locale;

/** Turns a folder name into an ID of the course format: {@code L&MC} becomes {@code l-mc}. */
final class Slug {

    private Slug() {
    }

    static String of(String name) {
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = plain.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "subject" : slug;
    }
}
