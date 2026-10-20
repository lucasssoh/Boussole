package io.github.lucasssoh.boussole.format.model.answer;

/** What an exercise accepts. One kind per exercise type, except free exercises, which have none. */
public sealed interface Answer permits NumericAnswer, TextAnswer, FillAnswer, OrderAnswer, MatchAnswer {
}
