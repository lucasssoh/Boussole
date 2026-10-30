package io.github.lucasssoh.boussole.core.device;

/** The owner removes a device: its key opens nothing any more. */
public final class RemoveDevice {

    private final DeviceRepository devices;

    public RemoveDevice(DeviceRepository devices) {
        this.devices = devices;
    }

    public void remove(String id) {
        if (!devices.remove(id)) {
            throw new PairingRefusedException("no paired device " + id);
        }
    }
}
