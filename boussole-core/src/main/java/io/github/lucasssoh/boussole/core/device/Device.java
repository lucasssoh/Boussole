package io.github.lucasssoh.boussole.core.device;

import java.time.Instant;

/**
 * Another device paired with this Boussole: its name and address as the server saw them (the
 * device never names itself), what it may do, when it was paired and last seen.
 */
public record Device(String id, String name, String address, DeviceAccess access, Instant pairedAt, Instant lastSeen) {
}
