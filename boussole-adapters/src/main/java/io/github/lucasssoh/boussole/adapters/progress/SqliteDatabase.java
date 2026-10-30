package io.github.lucasssoh.boussole.adapters.progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * The SQLite file, its schema brought up to date when opened. Each migration runs once, in order;
 * {@code PRAGMA user_version} remembers how many ran.
 */
public final class SqliteDatabase {

    private static final List<String> MIGRATIONS = List.of("V1__progress.sql", "V2__attempt_session.sql", "V3__points.sql", "V4__attempt_stars.sql");

    private final String url;

    private SqliteDatabase(Path file) {
        this.url = "jdbc:sqlite:" + file;
    }

    public static SqliteDatabase open(Path file) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        SqliteDatabase database = new SqliteDatabase(file);
        database.migrate();
        return database;
    }

    Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void migrate() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            int applied;
            try (var result = statement.executeQuery("PRAGMA user_version")) {
                applied = result.getInt(1);
            }
            for (int index = applied; index < MIGRATIONS.size(); index++) {
                connection.setAutoCommit(false);
                for (String sql : script(MIGRATIONS.get(index)).split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
                statement.execute("PRAGMA user_version = " + (index + 1));
                connection.commit();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("cannot prepare the progress database " + url, e);
        }
    }

    private static String script(String name) {
        try (InputStream in = SqliteDatabase.class.getResourceAsStream("/db/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing migration " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
