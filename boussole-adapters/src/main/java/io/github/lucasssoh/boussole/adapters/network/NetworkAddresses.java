package io.github.lucasssoh.boussole.adapters.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** The addresses other devices of the local network may reach this computer by. */
public final class NetworkAddresses {

    private NetworkAddresses() {
    }

    /** Its private IPv4 addresses, those of a home network ({@code 192.168…}) first. */
    public static List<String> current() {
        try {
            return NetworkInterface.networkInterfaces()
                    .filter(NetworkAddresses::isUsable)
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(address -> address instanceof Inet4Address && address.isSiteLocalAddress())
                    .map(InetAddress::getHostAddress)
                    .distinct()
                    .sorted(Comparator.comparing((String address) -> !address.startsWith("192.168.")).thenComparing(Comparator.naturalOrder()))
                    .toList();
        } catch (SocketException e) {
            return List.of();
        }
    }

    /**
     * The name this computer announces on the local network ({@code fedora.local}), which Linux
     * (Avahi), macOS and Windows answer to without Boussole announcing anything.
     */
    public static Optional<String> localName() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            String name = host.contains(".") ? host.substring(0, host.indexOf('.')) : host;
            return name.isBlank() || name.equals("localhost") ? Optional.empty() : Optional.of(name.toLowerCase(Locale.ROOT) + ".local");
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    private static boolean isUsable(NetworkInterface network) {
        try {
            return network.isUp() && !network.isLoopback() && !network.isVirtual();
        } catch (SocketException e) {
            return false;
        }
    }
}
