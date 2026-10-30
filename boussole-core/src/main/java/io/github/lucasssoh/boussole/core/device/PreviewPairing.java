package io.github.lucasssoh.boussole.core.device;

/** A device checks its code before pairing: the access it would get. A wrong code counts against the offer. */
public final class PreviewPairing {

    private final PairingDesk desk;

    public PreviewPairing(PairingDesk desk) {
        this.desk = desk;
    }

    public DeviceAccess preview(String code) {
        return desk.check(code);
    }
}
