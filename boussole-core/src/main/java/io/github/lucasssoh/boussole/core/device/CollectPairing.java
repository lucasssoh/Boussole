package io.github.lucasssoh.boussole.core.device;

/** The device that made a request asks where it stands; once accepted, it gets its key, once. */
public final class CollectPairing {

    private final PairingDesk desk;

    public CollectPairing(PairingDesk desk) {
        this.desk = desk;
    }

    public PairingStatus collect(String requestId) {
        return desk.collect(requestId);
    }
}
