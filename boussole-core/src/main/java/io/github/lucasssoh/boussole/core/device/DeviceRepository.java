package io.github.lucasssoh.boussole.core.device;

import java.util.List;
import java.util.Optional;

/** Where paired devices are kept, each with the hash of its key, never the key itself (port). */
public interface DeviceRepository {

    void add(Device device, String keyHash);

    Optional<Device> byKeyHash(String keyHash);

    /** Every paired device, the most recently seen first. */
    List<Device> all();

    Optional<Device> byId(String id);

    void update(Device device);

    /** Says whether there was such a device. */
    boolean remove(String id);
}
