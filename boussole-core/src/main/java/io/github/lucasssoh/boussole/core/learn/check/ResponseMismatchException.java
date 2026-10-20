package io.github.lucasssoh.boussole.core.learn.check;

/** The response does not fit the question: a list of choices for a numeric exercise, a missing item… */
public final class ResponseMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponseMismatchException(String message) {
        super(message);
    }
}
