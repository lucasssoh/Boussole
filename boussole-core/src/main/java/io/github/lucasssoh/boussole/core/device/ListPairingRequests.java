package io.github.lucasssoh.boussole.core.device;

import java.util.List;

/** The requests waiting for the owner, the oldest first. */
public final class ListPairingRequests {

    private final PairingDesk desk;

    public ListPairingRequests(PairingDesk desk) {
        this.desk = desk;
    }

    public List<PairingRequest> list() {
        return desk.pending();
    }
}
