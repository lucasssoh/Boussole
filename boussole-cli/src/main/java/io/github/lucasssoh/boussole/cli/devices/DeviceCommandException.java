package io.github.lucasssoh.boussole.cli.devices;

/** Why a {@code boussole devices} command could not be done, as the server or the network says it. */
public final class DeviceCommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    DeviceCommandException(String message) {
        super(message);
    }
}
