package io.github.lucasssoh.boussole.core.learn;

/** A subject, a session or an exercise that the courses do not have. */
public final class ContentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ContentNotFoundException(String message) {
        super(message);
    }
}
