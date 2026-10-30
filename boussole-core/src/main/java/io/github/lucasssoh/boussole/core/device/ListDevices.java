package io.github.lucasssoh.boussole.core.device;

import java.util.List;

/** The paired devices, the most recently seen first. */
public final class ListDevices {

    private final DeviceRepository devices;

    public ListDevices(DeviceRepository devices) {
        this.devices = devices;
    }

    public List<Device> list() {
        return devices.all();
    }
}
