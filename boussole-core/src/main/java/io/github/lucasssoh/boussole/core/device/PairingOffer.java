package io.github.lucasssoh.boussole.core.device;

import java.time.Instant;

/** A code the owner shows (typed, or in a QR code): the first device to give it is paired, with this access. */
public record PairingOffer(String code, DeviceAccess access, Instant expiresAt) {
}
