package io.github.lucasssoh.boussole.server.web;

import io.github.lucasssoh.boussole.core.device.Device;
import io.github.lucasssoh.boussole.core.device.DeviceAccess;
import io.github.lucasssoh.boussole.core.device.IdentifyDevice;
import io.github.lucasssoh.boussole.server.NetworkSettings;
import io.github.lucasssoh.boussole.server.ServerSettings;
import io.github.lucasssoh.boussole.server.device.DeviceCookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Who may ask what. On 127.0.0.1, this computer's browser: a web page elsewhere could otherwise
 * reach it, through another host name pointed at 127.0.0.1 (refused by the {@code Host} check),
 * or by posting a form to it (refused, since a form cannot send the {@code X-Boussole} header).
 * On the network port, another device: the same two checks against the computer's own names, then
 * nothing of the API without the key of a paired device, but pairing itself; nothing written by
 * a read-only device; and the devices and the server never managed from the network.
 */
@Component
class AccessFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Boussole";
    /** The device a network request comes from, for the controllers that want it. */
    static final String DEVICE = "boussole.device";
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");
    private static final Set<String> READS = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name());

    private final ServerSettings settings;
    private final IdentifyDevice identify;

    AccessFilter(ServerSettings settings, IdentifyDevice identify) {
        this.settings = settings;
        this.identify = identify;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<NetworkSettings> network = settings.network().filter(found -> found.port() == request.getLocalPort());
        Set<String> hosts = network.<Set<String>>map(found -> union(found.hosts())).orElse(LOCAL_HOSTS);
        if (!hosts.contains(hostOf(request.getHeader("Host")))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Boussole does not answer to this host name");
            return;
        }
        boolean reads = READS.contains(request.getMethod());
        if (!reads && request.getHeader(HEADER) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "missing " + HEADER + " header");
            return;
        }
        if (network.isPresent() && !allowedFromNetwork(request, response, reads)) {
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean allowedFromNetwork(HttpServletRequest request, HttpServletResponse response, boolean reads) throws IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/devices") || path.startsWith("/api/server/stop")) {
            refuse(response, HttpServletResponse.SC_FORBIDDEN, "only this computer manages devices and the server");
            return false;
        }
        if (path.startsWith("/api/pairing/") || !path.startsWith("/api/")) {
            return true;
        }
        Optional<Device> device = DeviceCookie.read(request).flatMap(key -> identify.identify(key, request.getRemoteAddr()));
        if (device.isEmpty()) {
            refuse(response, HttpServletResponse.SC_UNAUTHORIZED, "this device is not paired");
            return false;
        }
        if (!reads && device.get().access() == DeviceAccess.READ_ONLY) {
            refuse(response, HttpServletResponse.SC_FORBIDDEN, "this device is read-only");
            return false;
        }
        request.setAttribute(DEVICE, device.get());
        return true;
    }

    private static void refuse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private static Set<String> union(List<String> hosts) {
        Set<String> all = new HashSet<>(LOCAL_HOSTS);
        all.addAll(hosts);
        return all;
    }

    private static String hostOf(String host) {
        return Optional.ofNullable(host).map(value -> value.replaceFirst(":\\d+$", "")).orElse("");
    }
}
