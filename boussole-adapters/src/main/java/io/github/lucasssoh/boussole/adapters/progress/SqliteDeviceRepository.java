package io.github.lucasssoh.boussole.adapters.progress;

import io.github.lucasssoh.boussole.core.device.Device;
import io.github.lucasssoh.boussole.core.device.DeviceAccess;
import io.github.lucasssoh.boussole.core.device.DeviceRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The paired devices in SQLite, beside the progress, each with the hash of its key. */
public final class SqliteDeviceRepository implements DeviceRepository {

    private static final String COLUMNS = "id, name, address, access, paired_at, last_seen";

    private final SqliteDatabase database;

    public SqliteDeviceRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void add(Device device, String keyHash) {
        query("INSERT INTO device (" + COLUMNS + ", key_hash) VALUES (?, ?, ?, ?, ?, ?, ?)", statement -> {
            bind(statement, device);
            statement.setString(7, keyHash);
            return statement.executeUpdate();
        });
    }

    @Override
    public Optional<Device> byKeyHash(String keyHash) {
        return one("SELECT " + COLUMNS + " FROM device WHERE key_hash = ?", keyHash);
    }

    @Override
    public Optional<Device> byId(String id) {
        return one("SELECT " + COLUMNS + " FROM device WHERE id = ?", id);
    }

    @Override
    public List<Device> all() {
        return query("SELECT " + COLUMNS + " FROM device ORDER BY last_seen DESC", statement -> {
            List<Device> found = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(device(rows));
                }
            }
            return found;
        });
    }

    @Override
    public void update(Device device) {
        query("UPDATE device SET name = ?, address = ?, access = ?, last_seen = ? WHERE id = ?", statement -> {
            statement.setString(1, device.name());
            statement.setString(2, device.address());
            statement.setString(3, device.access().name());
            statement.setString(4, device.lastSeen().toString());
            statement.setString(5, device.id());
            return statement.executeUpdate();
        });
    }

    @Override
    public boolean remove(String id) {
        return query("DELETE FROM device WHERE id = ?", statement -> {
            statement.setString(1, id);
            return statement.executeUpdate();
        }) > 0;
    }

    private Optional<Device> one(String sql, String value) {
        return query(sql, statement -> {
            statement.setString(1, value);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(device(rows)) : Optional.<Device>empty();
            }
        });
    }

    private static void bind(PreparedStatement statement, Device device) throws SQLException {
        statement.setString(1, device.id());
        statement.setString(2, device.name());
        statement.setString(3, device.address());
        statement.setString(4, device.access().name());
        statement.setString(5, device.pairedAt().toString());
        statement.setString(6, device.lastSeen().toString());
    }

    private static Device device(ResultSet row) throws SQLException {
        return new Device(row.getString("id"), row.getString("name"), row.getString("address"),
                DeviceAccess.valueOf(row.getString("access")), Instant.parse(row.getString("paired_at")),
                Instant.parse(row.getString("last_seen")));
    }

    private <T> T query(String sql, StatementWork<T> work) {
        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            return work.run(statement);
        } catch (SQLException e) {
            throw new IllegalStateException("device database: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface StatementWork<T> {
        T run(PreparedStatement statement) throws SQLException;
    }
}
