package io.github.lucasssoh.boussole.core.learn.check;

import io.github.lucasssoh.boussole.format.model.answer.MatchAnswer;

/** Each left-hand side is checked: it is right when paired with its own right-hand side. */
final class MatchAnswerResponseChecker implements ResponseChecker<MatchAnswer, MatchResponse> {

    @Override
    public Class<MatchResponse> responseType() {
        return MatchResponse.class;
    }

    @Override
    public CheckResult check(MatchAnswer answer, MatchResponse response) {
        return CheckResult.ofItems(answer.pairs().stream()
                .map(pair -> CheckedItem.of(pair.left(), pair.right().equals(response.pairs().get(pair.left()))))
                .toList());
    }
}
