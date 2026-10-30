package io.github.lucasssoh.boussole.core.device;

/** A pairing that cannot go on: no code offered, a wrong or expired code, too many requests. */
public final class PairingRefusedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PairingRefusedException(String message) {
        super(message);
    }
}
