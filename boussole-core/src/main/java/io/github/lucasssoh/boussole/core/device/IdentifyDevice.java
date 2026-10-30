package io.github.lucasssoh.boussole.core.device;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** The paired device a key belongs to, if any; it is marked seen, at most once a minute. */
public final class IdentifyDevice {

    private static final Duration SEEN_EVERY = Duration.ofMinutes(1);

    private final DeviceRepository devices;
    private final Clock clock;

    public IdentifyDevice(DeviceRepository devices, Clock clock) {
        this.devices = devices;
        this.clock = clock;
    }

    public Optional<Device> identify(String key, String address) {
        return devices.byKeyHash(DeviceKeys.hash(key)).map(device -> {
            Instant now = clock.instant();
            if (!device.address().equals(address) || !now.isBefore(device.lastSeen().plus(SEEN_EVERY))) {
                Device seen = new Device(device.id(), device.name(), address, device.access(), device.pairedAt(), now);
                devices.update(seen);
                return seen;
            }
            return device;
        });
    }
}
