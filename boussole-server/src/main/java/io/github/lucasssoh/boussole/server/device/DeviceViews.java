package io.github.lucasssoh.boussole.server.device;

import io.github.lucasssoh.boussole.core.device.Device;
import io.github.lucasssoh.boussole.core.device.DeviceAccess;
import io.github.lucasssoh.boussole.core.device.PairingRefusedException;
import io.github.lucasssoh.boussole.core.device.PairingRequest;
import java.time.Instant;
import java.util.Locale;

/** What the API says of devices; an access reads {@code full} or {@code read-only}. */
final class DeviceViews {

    private DeviceViews() {
    }

    record DeviceView(String id, String name, String address, String access, Instant pairedAt, Instant lastSeen) {

        static DeviceView of(Device device) {
            return new DeviceView(device.id(), device.name(), device.address(), accessName(device.access()), device.pairedAt(), device.lastSeen());
        }
    }

    /** A request as the owner sees it: no ID, which only the device that made it knows. */
    record RequestView(String code, String name, String address, Instant expiresAt) {

        static RequestView of(PairingRequest request) {
            return new RequestView(request.code(), request.name(), request.address(), request.expiresAt());
        }
    }

    static String accessName(DeviceAccess access) {
        return access.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    static DeviceAccess access(String name) {
        for (DeviceAccess access : DeviceAccess.values()) {
            if (accessName(access).equals(name)) {
                return access;
            }
        }
        throw new PairingRefusedException("unknown access " + name + ": full or read-only");
    }
}
