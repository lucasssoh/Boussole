package io.github.lucasssoh.boussole.server.device;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The name a device is shown with, as Bluetooth does: never one the device gives itself, but the
 * one the local network knows its address by (most home routers name each device), else what its
 * browser says of it ({@code Android · Chrome}).
 */
final class DeviceNames {

    private static final List<Map.Entry<String, String>> SYSTEMS = List.of(
            Map.entry("SMART-TV", "TV"), Map.entry("SmartTV", "TV"), Map.entry("Tizen", "TV"), Map.entry("Web0S", "TV"),
            Map.entry("Android", "Android"), Map.entry("iPhone", "iPhone"), Map.entry("iPad", "iPad"),
            Map.entry("CrOS", "Chromebook"), Map.entry("Windows", "Windows"), Map.entry("Macintosh", "Mac"), Map.entry("Linux", "Linux"));
    private static final List<Map.Entry<String, String>> BROWSERS = List.of(
            Map.entry("Firefox/", "Firefox"), Map.entry("Edg/", "Edge"), Map.entry("Chrome/", "Chrome"), Map.entry("Safari/", "Safari"));

    private DeviceNames() {
    }

    static String of(String address, String userAgent) {
        return networkName(address).orElseGet(() -> fromBrowser(Optional.ofNullable(userAgent).orElse("")));
    }

    private static Optional<String> networkName(String address) {
        try {
            String name = InetAddress.getByName(address).getCanonicalHostName();
            if (name.equals(address)) {
                return Optional.empty();
            }
            return Optional.of(name.contains(".") ? name.substring(0, name.indexOf('.')) : name).filter(found -> !found.isBlank());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    static String fromBrowser(String userAgent) {
        String system = first(SYSTEMS, userAgent).orElse("Device");
        return first(BROWSERS, userAgent).map(browser -> system + " · " + browser).orElse(system);
    }

    private static Optional<String> first(List<Map.Entry<String, String>> known, String userAgent) {
        return known.stream().filter(entry -> userAgent.contains(entry.getKey())).map(Map.Entry::getValue).findFirst();
    }
}
