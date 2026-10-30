package io.github.lucasssoh.boussole.adapters.progress;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.core.device.Device;
import io.github.lucasssoh.boussole.core.device.DeviceAccess;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteDeviceRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-10-30T16:00:00Z");
    private static final Device PHONE = new Device("d1", "Pixel-8", "192.168.1.23", DeviceAccess.FULL, NOW, NOW);
    private static final Device TV = new Device("d2", "Salon-TV", "192.168.1.31", DeviceAccess.READ_ONLY, NOW, NOW.plusSeconds(60));

    @TempDir
    Path folder;

    private SqliteDeviceRepository repository() {
        return new SqliteDeviceRepository(SqliteDatabase.open(folder.resolve("data/state.db")));
    }

    @Test
    void finds_a_device_by_the_hash_of_its_key_across_openings() {
        repository().add(PHONE, "hash-1");

        assertThat(repository().byKeyHash("hash-1")).contains(PHONE);
        assertThat(repository().byKeyHash("hash-2")).isEmpty();
        assertThat(repository().byId("d1")).contains(PHONE);
    }

    @Test
    void lists_the_most_recently_seen_first_and_keeps_edits() {
        repository().add(PHONE, "hash-1");
        repository().add(TV, "hash-2");
        Device renamed = new Device("d1", "Phone", "192.168.1.50", DeviceAccess.READ_ONLY, NOW, NOW.plusSeconds(120));
        repository().update(renamed);

        assertThat(repository().all()).containsExactly(renamed, TV);
    }

    @Test
    void a_removed_device_is_gone() {
        repository().add(PHONE, "hash-1");

        assertThat(repository().remove("d1")).isTrue();
        assertThat(repository().remove("d1")).isFalse();
        assertThat(repository().byKeyHash("hash-1")).isEmpty();
    }
}
