package io.github.lucasssoh.boussole.core.device;

/** A device just paired, and its key: the only time the key is known, given once to the device. */
public record PairedDevice(Device device, String key) {
}
