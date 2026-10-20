package io.github.lucasssoh.boussole.core.learn.check;

/** A free exercise cannot be checked: the learner reads the solution and grades themself. */
public record SelfGradeResponse(SelfGrade grade) implements LearnerResponse {
}
