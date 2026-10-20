package io.github.lucasssoh.boussole.format.parse;

/** An attribute whose value cannot be used. Reported as F027, and the directive is dropped. */
public final class InvalidAttributeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidAttributeException(String attribute, String problem) {
        super("`" + attribute + "` " + problem);
    }
}
