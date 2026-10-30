package io.github.lucasssoh.boussole.core.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PairingTest {

    private final MovingClock clock = new MovingClock();
    private final InMemoryDeviceRepository devices = new InMemoryDeviceRepository();
    private final PairingDesk desk = new PairingDesk(devices, clock);

    @Test
    void a_code_offered_pairs_one_device_once() {
        PairingOffer offer = new OfferPairing(desk).offer(DeviceAccess.FULL);

        assertThat(new PreviewPairing(desk).preview(offer.code())).isEqualTo(DeviceAccess.FULL);
        PairedDevice paired = new ClaimPairing(desk).claim(offer.code(), "Pixel-8", "192.168.1.23");

        assertThat(paired.device().name()).isEqualTo("Pixel-8");
        assertThat(new IdentifyDevice(devices, clock).identify(paired.key(), "192.168.1.23")).contains(paired.device());
        assertThatThrownBy(() -> new ClaimPairing(desk).claim(offer.code(), "Other", "192.168.1.40"))
                .isInstanceOf(PairingRefusedException.class);
    }

    @Test
    void a_code_may_be_typed_with_spaces() {
        PairingOffer offer = new OfferPairing(desk).offer(DeviceAccess.READ_ONLY);
        String spaced = offer.code().substring(0, 3) + " " + offer.code().substring(3);

        assertThat(new ClaimPairing(desk).claim(spaced, "Salon-TV", "192.168.1.31").device().access()).isEqualTo(DeviceAccess.READ_ONLY);
    }

    @Test
    void a_code_expires() {
        PairingOffer offer = new OfferPairing(desk).offer(DeviceAccess.FULL);
        clock.move(PairingDesk.LIFETIME);

        assertThatThrownBy(() -> new ClaimPairing(desk).claim(offer.code(), "Pixel-8", "192.168.1.23"))
                .isInstanceOf(PairingRefusedException.class)
                .hasMessageContaining("no pairing code is open");
    }

    @Test
    void wrong_codes_withdraw_the_offer() {
        PairingOffer offer = new OfferPairing(desk).offer(DeviceAccess.FULL);
        String wrong = offer.code().equals("000000") ? "111111" : "000000";
        for (int attempt = 0; attempt < PairingDesk.WRONG_CODES; attempt++) {
            assertThatThrownBy(() -> new PreviewPairing(desk).preview(wrong)).hasMessage("wrong pairing code");
        }

        assertThatThrownBy(() -> new ClaimPairing(desk).claim(offer.code(), "Pixel-8", "192.168.1.23"))
                .hasMessageContaining("no pairing code is open");
        assertThat(devices.all()).isEmpty();
    }

    @Test
    void a_request_accepted_hands_its_key_once() {
        PairingRequest request = new RequestPairing(desk).request("Salon-TV", "192.168.1.31");
        assertThat(new CollectPairing(desk).collect(request.id()).state()).isEqualTo(PairingStatus.State.PENDING);
        assertThat(new ListPairingRequests(desk).list()).containsExactly(request);

        new AnswerPairingRequest(desk).answer(request.code(), Optional.of(DeviceAccess.READ_ONLY));
        PairingStatus collected = new CollectPairing(desk).collect(request.id());

        assertThat(collected.state()).isEqualTo(PairingStatus.State.ACCEPTED);
        assertThat(collected.paired().orElseThrow().device().access()).isEqualTo(DeviceAccess.READ_ONLY);
        assertThat(new CollectPairing(desk).collect(request.id()).paired()).isEmpty();
        assertThat(new ListPairingRequests(desk).list()).isEmpty();
    }

    @Test
    void a_request_denied_pairs_nothing() {
        PairingRequest request = new RequestPairing(desk).request("Salon-TV", "192.168.1.31");
        new AnswerPairingRequest(desk).answer(request.code(), Optional.empty());

        assertThat(new CollectPairing(desk).collect(request.id()).state()).isEqualTo(PairingStatus.State.DENIED);
        assertThat(devices.all()).isEmpty();
    }

    @Test
    void a_request_accepted_but_never_collected_leaves_no_device() {
        PairingRequest request = new RequestPairing(desk).request("Salon-TV", "192.168.1.31");
        new AnswerPairingRequest(desk).answer(request.code(), Optional.of(DeviceAccess.FULL));
        clock.move(PairingDesk.LIFETIME);

        assertThat(new CollectPairing(desk).collect(request.id()).state()).isEqualTo(PairingStatus.State.EXPIRED);
        assertThat(devices.all()).isEmpty();
    }

    @Test
    void only_a_few_requests_wait_at_a_time() {
        for (int request = 0; request < PairingDesk.PENDING_REQUESTS; request++) {
            new RequestPairing(desk).request("TV-" + request, "192.168.1." + request);
        }

        assertThatThrownBy(() -> new RequestPairing(desk).request("TV", "192.168.1.99")).isInstanceOf(PairingRefusedException.class);
        clock.move(PairingDesk.LIFETIME);
        assertThat(new RequestPairing(desk).request("TV", "192.168.1.99").name()).isEqualTo("TV");
    }

    @Test
    void a_removed_device_is_not_recognised_and_the_owner_names_devices() {
        PairedDevice paired = new ClaimPairing(desk).claim(new OfferPairing(desk).offer(DeviceAccess.FULL).code(), "Pixel-8", "192.168.1.23");

        Device renamed = new EditDevice(devices).edit(paired.device().id(), Optional.of("Phone"), Optional.of(DeviceAccess.READ_ONLY));
        assertThat(renamed.name()).isEqualTo("Phone");
        assertThat(new ListDevices(devices).list()).containsExactly(renamed);

        new RemoveDevice(devices).remove(paired.device().id());
        assertThat(new IdentifyDevice(devices, clock).identify(paired.key(), "192.168.1.23")).isEmpty();
        assertThat(new IdentifyDevice(devices, clock).identify("not a key", "192.168.1.23")).isEmpty();
    }

    @Test
    void a_device_is_marked_seen_with_its_new_address() {
        PairedDevice paired = new ClaimPairing(desk).claim(new OfferPairing(desk).offer(DeviceAccess.FULL).code(), "Pixel-8", "192.168.1.23");
        clock.move(Duration.ofMinutes(5));

        Device seen = new IdentifyDevice(devices, clock).identify(paired.key(), "192.168.1.50").orElseThrow();

        assertThat(seen.address()).isEqualTo("192.168.1.50");
        assertThat(seen.lastSeen()).isEqualTo(clock.instant());
    }

    private static final class MovingClock extends Clock {

        private Instant now = Instant.parse("2026-10-30T16:00:00Z");

        void move(Duration by) {
            now = now.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
