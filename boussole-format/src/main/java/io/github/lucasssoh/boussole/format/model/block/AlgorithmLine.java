package io.github.lucasssoh.boussole.format.model.block;

/**
 * One line of an algorithm: how deep it is indented ({@code 0} for the outermost) and its text,
 * which may hold {@code $…$} math and end with a comment after {@code //} or {@code ▷}.
 */
public record AlgorithmLine(int depth, String text) {
}
