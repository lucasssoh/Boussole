package io.github.lucasssoh.boussole.core.device;

import java.util.Optional;

/** Where a request stands, and once accepted the paired device with its key, handed over once. */
public record PairingStatus(State state, Optional<PairedDevice> paired) {

    public enum State {
        PENDING,
        ACCEPTED,
        DENIED,
        EXPIRED
    }

    static PairingStatus of(State state) {
        return new PairingStatus(state, Optional.empty());
    }
}
