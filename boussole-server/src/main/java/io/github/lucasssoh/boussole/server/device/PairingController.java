package io.github.lucasssoh.boussole.server.device;

import io.github.lucasssoh.boussole.core.device.ClaimPairing;
import io.github.lucasssoh.boussole.core.device.CollectPairing;
import io.github.lucasssoh.boussole.core.device.PairedDevice;
import io.github.lucasssoh.boussole.core.device.PairingRefusedException;
import io.github.lucasssoh.boussole.core.device.PairingRequest;
import io.github.lucasssoh.boussole.core.device.PairingStatus;
import io.github.lucasssoh.boussole.core.device.PreviewPairing;
import io.github.lucasssoh.boussole.core.device.RequestPairing;
import io.github.lucasssoh.boussole.server.ServerSettings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Another device pairs itself, from the network port only: with the code the owner shows, or by
 * asking and waiting for the owner to accept. Its name is the one the network knows it by.
 */
@RestController
@RequestMapping("/api/pairing")
class PairingController {

    private final ServerSettings settings;
    private final PreviewPairing preview;
    private final ClaimPairing claim;
    private final RequestPairing request;
    private final CollectPairing collect;

    PairingController(ServerSettings settings, PreviewPairing preview, ClaimPairing claim, RequestPairing request, CollectPairing collect) {
        this.settings = settings;
        this.preview = preview;
        this.claim = claim;
        this.request = request;
        this.collect = collect;
    }

    @PostMapping("/preview")
    PairingView preview(@RequestBody CodeBody body, HttpServletRequest http) {
        fromNetwork(http);
        return new PairingView(nameOf(http), http.getRemoteAddr(), DeviceViews.accessName(preview.preview(body.code())));
    }

    /** The key comes back in a cookie only: the page never sees it. */
    @PostMapping("/claim")
    PairingView claim(@RequestBody CodeBody body, HttpServletRequest http, HttpServletResponse response) {
        fromNetwork(http);
        PairedDevice paired = claim.claim(body.code(), nameOf(http), http.getRemoteAddr());
        response.addHeader(HttpHeaders.SET_COOKIE, DeviceCookie.header(paired.key()));
        return PairingView.of(paired);
    }

    @PostMapping("/requests")
    RequestMadeView request(HttpServletRequest http) {
        fromNetwork(http);
        PairingRequest made = request.request(nameOf(http), http.getRemoteAddr());
        return new RequestMadeView(made.id(), made.code(), made.name(), made.address(), made.expiresAt());
    }

    @GetMapping("/requests/{id}")
    StatusView status(@PathVariable String id, HttpServletRequest http, HttpServletResponse response) {
        fromNetwork(http);
        PairingStatus status = collect.collect(id);
        status.paired().ifPresent(paired -> response.addHeader(HttpHeaders.SET_COOKIE, DeviceCookie.header(paired.key())));
        return new StatusView(status.state().name().toLowerCase(Locale.ROOT), status.paired().map(PairingView::of).orElse(null));
    }

    private void fromNetwork(HttpServletRequest http) {
        if (settings.network().filter(network -> network.port() == http.getLocalPort()).isEmpty()) {
            throw new PairingRefusedException("pairing is for other devices, on the network address");
        }
    }

    private static String nameOf(HttpServletRequest http) {
        return DeviceNames.of(http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT));
    }

    record CodeBody(String code) {
    }

    record PairingView(String name, String address, String access) {

        static PairingView of(PairedDevice paired) {
            return new PairingView(paired.device().name(), paired.device().address(), DeviceViews.accessName(paired.device().access()));
        }
    }

    record RequestMadeView(String id, String code, String name, String address, Instant expiresAt) {
    }

    /** {@code state}: pending, accepted, denied or expired. */
    record StatusView(String state, PairingView device) {
    }
}
