package io.github.lucasssoh.boussole.core.device;

/** A device without a keyboard asks to be paired: it shows a code, for the owner to accept. */
public final class RequestPairing {

    private final PairingDesk desk;

    public RequestPairing(PairingDesk desk) {
        this.desk = desk;
    }

    public PairingRequest request(String name, String address) {
        return desk.request(name, address);
    }
}
