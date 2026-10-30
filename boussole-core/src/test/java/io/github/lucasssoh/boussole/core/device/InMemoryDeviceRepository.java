package io.github.lucasssoh.boussole.core.device;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class InMemoryDeviceRepository implements DeviceRepository {

    private final Map<String, Device> devices = new LinkedHashMap<>();
    private final Map<String, String> hashes = new LinkedHashMap<>();

    @Override
    public void add(Device device, String keyHash) {
        devices.put(device.id(), device);
        hashes.put(keyHash, device.id());
    }

    @Override
    public Optional<Device> byKeyHash(String keyHash) {
        return Optional.ofNullable(hashes.get(keyHash)).map(devices::get);
    }

    @Override
    public List<Device> all() {
        return devices.values().stream().sorted(Comparator.comparing(Device::lastSeen).reversed()).toList();
    }

    @Override
    public Optional<Device> byId(String id) {
        return Optional.ofNullable(devices.get(id));
    }

    @Override
    public void update(Device device) {
        devices.put(device.id(), device);
    }

    @Override
    public boolean remove(String id) {
        hashes.values().remove(id);
        return devices.remove(id) != null;
    }
}
