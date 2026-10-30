package io.github.lucasssoh.boussole.core.device;

import java.util.Optional;

/** The owner accepts, with an access, or denies the request that shows a code. */
public final class AnswerPairingRequest {

    private final PairingDesk desk;

    public AnswerPairingRequest(PairingDesk desk) {
        this.desk = desk;
    }

    /** {@code accepted} empty: denied. */
    public PairingRequest answer(String code, Optional<DeviceAccess> accepted) {
        return desk.answer(code, accepted);
    }
}
