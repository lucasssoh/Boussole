package io.github.lucasssoh.boussole.format.parse.answer;

/** A non-blank line of an {@code answer} part, stripped, with its line number. */
public record AnswerLine(String text, int line) {
}
