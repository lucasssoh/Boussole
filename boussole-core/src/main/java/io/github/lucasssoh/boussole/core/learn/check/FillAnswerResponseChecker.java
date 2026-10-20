package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;

/** Each gap is checked on its own; an empty gap is wrong. */
final class FillAnswerResponseChecker implements ResponseChecker<FillAnswer, GapsResponse> {

    @Override
    public Class<GapsResponse> responseType() {
        return GapsResponse.class;
    }

    @Override
    public CheckResult check(FillAnswer answer, GapsResponse response) {
        return CheckResult.ofItems(answer.gaps().stream()
                .map(gap -> CheckedItem.of(gap.number(),
                        ExpectationMatcher.matches(gap.expected(), response.values().getOrDefault(gap.number(), ""))))
                .toList());
    }
}
