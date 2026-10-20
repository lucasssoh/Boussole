package io.github.lucasssoh.boussole.core.learn.check;

/**
 * Checks one kind of response against what a question expects. There is one checker per kind of
 * question, chosen by {@link QuestionChecker}.
 *
 * @param <E> what is expected: an answer, or the question itself when it has no separate answer
 * @param <R> the kind of response this checker takes
 */
public interface ResponseChecker<E, R extends LearnerResponse> {

    Class<R> responseType();

    CheckResult check(E expected, R response);

    /** Checks a response of any kind, refusing one this checker does not take. */
    default CheckResult checkResponse(E expected, LearnerResponse response) {
        if (!responseType().isInstance(response)) {
            throw new ResponseMismatchException(
                    "expected a " + responseType().getSimpleName() + ", got a " + response.getClass().getSimpleName());
        }
        return check(expected, responseType().cast(response));
    }
}
