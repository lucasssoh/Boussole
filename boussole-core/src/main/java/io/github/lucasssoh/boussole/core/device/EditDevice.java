package io.github.lucasssoh.boussole.core.device;

import java.util.Optional;

/** The owner renames a device or changes its access; only the owner names devices. */
public final class EditDevice {

    private final DeviceRepository devices;

    public EditDevice(DeviceRepository devices) {
        this.devices = devices;
    }

    public Device edit(String id, Optional<String> name, Optional<DeviceAccess> access) {
        Device device = devices.byId(id).orElseThrow(() -> new PairingRefusedException("no paired device " + id));
        Device edited = new Device(device.id(), name.map(String::strip).filter(found -> !found.isEmpty()).orElse(device.name()),
                device.address(), access.orElse(device.access()), device.pairedAt(), device.lastSeen());
        devices.update(edited);
        return edited;
    }
}
