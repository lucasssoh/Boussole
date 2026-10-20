package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.answer.SingleAnswer;

/** A numeric or text exercise: the value typed is one of the accepted ones. */
final class SingleAnswerResponseChecker implements ResponseChecker<SingleAnswer, ValueResponse> {

    @Override
    public Class<ValueResponse> responseType() {
        return ValueResponse.class;
    }

    @Override
    public CheckResult check(SingleAnswer answer, ValueResponse response) {
        return CheckResult.whole(ExpectationMatcher.matches(answer.expected(), response.value()));
    }
}
