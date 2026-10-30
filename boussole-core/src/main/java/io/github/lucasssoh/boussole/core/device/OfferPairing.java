package io.github.lucasssoh.boussole.core.device;

/** The owner opens a pairing: a code, valid a short while, that one device may give to be paired. */
public final class OfferPairing {

    private final PairingDesk desk;

    public OfferPairing(PairingDesk desk) {
        this.desk = desk;
    }

    /** Replaces any code still open. */
    public PairingOffer offer(DeviceAccess access) {
        return desk.offer(access);
    }
}
