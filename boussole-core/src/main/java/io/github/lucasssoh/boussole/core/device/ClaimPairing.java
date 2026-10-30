package io.github.lucasssoh.boussole.core.device;

/** A device gives the code offered: it is paired under the name and address the server saw, and gets its key. */
public final class ClaimPairing {

    private final PairingDesk desk;

    public ClaimPairing(PairingDesk desk) {
        this.desk = desk;
    }

    public PairedDevice claim(String code, String name, String address) {
        return desk.claim(code, name, address);
    }
}
