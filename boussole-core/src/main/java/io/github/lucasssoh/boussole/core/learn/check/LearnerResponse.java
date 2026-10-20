package io.github.lucasssoh.boussole.core.learn.check;

/** What the learner gave for a question. Each kind of question takes one kind of response. */
public sealed interface LearnerResponse
        permits ChoicesResponse, ValueResponse, GapsResponse, OrderResponse, MatchResponse, SelfGradeResponse {
}
