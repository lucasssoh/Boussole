package io.github.lucasssoh.boussole.core.device;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The pairings under way, kept in memory only: a restart forgets them. One offer at a time, made
 * by the owner; requests made by devices, a few at a time. Codes last {@link #LIFETIME} and serve
 * once; an offer is withdrawn after {@link #WRONG_CODES} wrong codes, so that six digits cannot be
 * guessed.
 */
public final class PairingDesk {

    public static final Duration LIFETIME = Duration.ofMinutes(2);
    public static final int WRONG_CODES = 5;
    public static final int PENDING_REQUESTS = 5;

    private final DeviceRepository devices;
    private final Clock clock;
    private Offer offer;
    private final Map<String, Request> requests = new LinkedHashMap<>();

    public PairingDesk(DeviceRepository devices, Clock clock) {
        this.devices = devices;
        this.clock = clock;
    }

    synchronized PairingOffer offer(DeviceAccess access) {
        offer = new Offer(DeviceKeys.newCode(), access, clock.instant().plus(LIFETIME), WRONG_CODES);
        return offer.view();
    }

    synchronized DeviceAccess check(String code) {
        return liveOffer(code).access();
    }

    synchronized PairedDevice claim(String code, String name, String address) {
        DeviceAccess access = liveOffer(code).access();
        offer = null;
        return pair(name, address, access);
    }

    synchronized PairingRequest request(String name, String address) {
        purge();
        long pending = requests.values().stream().filter(request -> request.state == PairingStatus.State.PENDING).count();
        if (pending >= PENDING_REQUESTS) {
            throw new PairingRefusedException("too many pairing requests: try again in a few minutes");
        }
        Request request = new Request(DeviceKeys.newId(), uniqueCode(), name, address, clock.instant().plus(LIFETIME));
        requests.put(request.id, request);
        return request.view();
    }

    synchronized List<PairingRequest> pending() {
        purge();
        return requests.values().stream()
                .filter(request -> request.state == PairingStatus.State.PENDING)
                .sorted(Comparator.comparing(request -> request.expiresAt))
                .map(Request::view)
                .toList();
    }

    synchronized PairingRequest answer(String code, Optional<DeviceAccess> accepted) {
        purge();
        Request request = requests.values().stream()
                .filter(found -> found.state == PairingStatus.State.PENDING && DeviceKeys.sameCode(found.code, digits(code)))
                .findFirst()
                .orElseThrow(() -> new PairingRefusedException("no pending request shows the code " + code));
        if (accepted.isPresent()) {
            request.paired = pair(request.name, request.address, accepted.get());
            request.state = PairingStatus.State.ACCEPTED;
        } else {
            request.state = PairingStatus.State.DENIED;
        }
        request.expiresAt = clock.instant().plus(LIFETIME);
        return request.view();
    }

    synchronized PairingStatus collect(String id) {
        purge();
        Request request = requests.get(id);
        if (request == null) {
            return PairingStatus.of(PairingStatus.State.EXPIRED);
        }
        if (request.state == PairingStatus.State.PENDING) {
            return PairingStatus.of(PairingStatus.State.PENDING);
        }
        requests.remove(id);
        return new PairingStatus(request.state, Optional.ofNullable(request.paired));
    }

    private Offer liveOffer(String code) {
        Instant now = clock.instant();
        if (offer == null || !now.isBefore(offer.expiresAt)) {
            offer = null;
            throw new PairingRefusedException("no pairing code is open: show a new one on the computer");
        }
        if (!DeviceKeys.sameCode(offer.code, digits(code))) {
            offer = offer.wrongLeft <= 1 ? null : offer.wrong();
            throw new PairingRefusedException("wrong pairing code");
        }
        return offer;
    }

    /** A code as typed: {@code 382 615} is {@code 382615}. */
    private static String digits(String code) {
        return code.replaceAll("\\s", "");
    }

    private PairedDevice pair(String name, String address, DeviceAccess access) {
        Instant now = clock.instant();
        Device device = new Device(DeviceKeys.newId(), name, address, access, now, now);
        String key = DeviceKeys.newKey();
        devices.add(device, DeviceKeys.hash(key));
        return new PairedDevice(device, key);
    }

    /** Two pending requests never show the same code, nor the offer's. */
    private String uniqueCode() {
        while (true) {
            String code = DeviceKeys.newCode();
            boolean taken = requests.values().stream().anyMatch(request -> request.code.equals(code))
                    || offer != null && offer.code.equals(code);
            if (!taken) {
                return code;
            }
        }
    }

    private void purge() {
        Instant now = clock.instant();
        new ArrayList<>(requests.values()).stream()
                .filter(request -> !now.isBefore(request.expiresAt))
                .forEach(request -> {
                    requests.remove(request.id);
                    // Accepted but never collected: nobody holds its key, the device goes.
                    if (request.paired != null) {
                        devices.remove(request.paired.device().id());
                    }
                });
    }

    private record Offer(String code, DeviceAccess access, Instant expiresAt, int wrongLeft) {

        Offer wrong() {
            return new Offer(code, access, expiresAt, wrongLeft - 1);
        }

        PairingOffer view() {
            return new PairingOffer(code, access, expiresAt);
        }
    }

    private static final class Request {

        final String id;
        final String code;
        final String name;
        final String address;
        Instant expiresAt;
        PairingStatus.State state = PairingStatus.State.PENDING;
        PairedDevice paired;

        Request(String id, String code, String name, String address, Instant expiresAt) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.address = address;
            this.expiresAt = expiresAt;
        }

        PairingRequest view() {
            return new PairingRequest(id, code, name, address, expiresAt);
        }
    }
}
