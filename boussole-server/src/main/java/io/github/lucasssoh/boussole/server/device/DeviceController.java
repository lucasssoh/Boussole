package io.github.lucasssoh.boussole.server.device;

import io.github.lucasssoh.boussole.core.device.AnswerPairingRequest;
import io.github.lucasssoh.boussole.core.device.EditDevice;
import io.github.lucasssoh.boussole.core.device.ListDevices;
import io.github.lucasssoh.boussole.core.device.ListPairingRequests;
import io.github.lucasssoh.boussole.core.device.OfferPairing;
import io.github.lucasssoh.boussole.core.device.PairingOffer;
import io.github.lucasssoh.boussole.core.device.PairingRefusedException;
import io.github.lucasssoh.boussole.core.device.RemoveDevice;
import io.github.lucasssoh.boussole.server.NetworkSettings;
import io.github.lucasssoh.boussole.server.ServerSettings;
import io.github.lucasssoh.boussole.server.device.DeviceViews.DeviceView;
import io.github.lucasssoh.boussole.server.device.DeviceViews.RequestView;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The owner manages devices, from this computer only ({@code boussole devices}). */
@RestController
@RequestMapping("/api/devices")
class DeviceController {

    private final ServerSettings settings;
    private final ListDevices list;
    private final ListPairingRequests requests;
    private final OfferPairing offer;
    private final AnswerPairingRequest answer;
    private final EditDevice edit;
    private final RemoveDevice remove;

    DeviceController(ServerSettings settings, ListDevices list, ListPairingRequests requests, OfferPairing offer,
            AnswerPairingRequest answer, EditDevice edit, RemoveDevice remove) {
        this.settings = settings;
        this.list = list;
        this.requests = requests;
        this.offer = offer;
        this.answer = answer;
        this.edit = edit;
        this.remove = remove;
    }

    @GetMapping
    DevicesView devices() {
        return new DevicesView(
                settings.network().map(NetworkView::of).orElse(NetworkView.OFF),
                requests.list().stream().map(RequestView::of).toList(),
                list.list().stream().map(DeviceView::of).toList());
    }

    /** A code to pair one device, and the addresses that lead to it ({@code ?code=} fills it in). */
    @PostMapping("/pairing")
    OfferView pair(@RequestBody AccessBody body) {
        NetworkSettings network = settings.network()
                .orElseThrow(() -> new PairingRefusedException("network access is off: set network: true in config.yaml, then restart Boussole"));
        PairingOffer made = offer.offer(DeviceViews.access(body.access()));
        List<String> urls = network.hosts().stream().map(host -> "https://" + host + ":" + network.port() + "/pair?code=" + made.code()).toList();
        return new OfferView(made.code(), DeviceViews.accessName(made.access()), made.expiresAt(), urls);
    }

    @PostMapping("/requests/{code}/accept")
    RequestView accept(@PathVariable String code, @RequestBody AccessBody body) {
        return RequestView.of(answer.answer(code, Optional.of(DeviceViews.access(body.access()))));
    }

    @PostMapping("/requests/{code}/deny")
    RequestView deny(@PathVariable String code) {
        return RequestView.of(answer.answer(code, Optional.empty()));
    }

    @PatchMapping("/{id}")
    DeviceView edit(@PathVariable String id, @RequestBody EditBody body) {
        return DeviceView.of(edit.edit(id, Optional.ofNullable(body.name()), Optional.ofNullable(body.access()).map(DeviceViews::access)));
    }

    @DeleteMapping("/{id}")
    void remove(@PathVariable String id) {
        remove.remove(id);
    }

    record AccessBody(String access) {
    }

    record EditBody(String name, String access) {
    }

    record DevicesView(NetworkView network, List<RequestView> requests, List<DeviceView> devices) {
    }

    record OfferView(String code, String access, Instant expiresAt, List<String> urls) {
    }

    /** {@code fingerprint}: the certificate's SHA-256, to compare with what the device's browser shows. */
    record NetworkView(boolean enabled, Integer port, List<String> hosts, String fingerprint) {

        static final NetworkView OFF = new NetworkView(false, null, List.of(), null);

        static NetworkView of(NetworkSettings network) {
            return new NetworkView(true, network.port(), network.hosts(), network.fingerprint());
        }
    }
}
