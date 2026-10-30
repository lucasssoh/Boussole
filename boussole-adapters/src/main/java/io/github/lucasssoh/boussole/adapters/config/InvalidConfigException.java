package io.github.lucasssoh.boussole.adapters.config;

/** {@code config.yaml} cannot be read: where, and what is wrong, in one line. */
public final class InvalidConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    InvalidConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
