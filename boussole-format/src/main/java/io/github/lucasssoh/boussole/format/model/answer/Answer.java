package io.github.lucasssoh.boussole.format.model.answer;

/**
 * What an exercise accepts. Numeric and text exercises expect one value, fill exercises one per
 * gap; order and match exercises an arrangement. Free exercises have none.
 */
public sealed interface Answer permits SingleAnswer, FillAnswer, OrderAnswer, MatchAnswer {
}
