package io.github.lucasssoh.boussole.core.device;

import java.time.Instant;

/**
 * A device asking to be paired, for one without a keyboard: it shows {@code code}, the owner
 * accepts the request with the same code. {@code id} is known only to that device, which collects
 * its key with it.
 */
public record PairingRequest(String id, String code, String name, String address, Instant expiresAt) {
}
