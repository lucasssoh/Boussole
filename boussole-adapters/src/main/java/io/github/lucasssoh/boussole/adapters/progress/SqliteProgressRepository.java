package io.github.lucasssoh.boussole.adapters.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The learner's progress in SQLite. Each attempt is also kept in a history, for statistics to come. */
public final class SqliteProgressRepository implements ProgressRepository {

    private static final String COLUMNS = "exercise, attempts, last_verdict, solved, hints_revealed";

    private final SqliteDatabase database;

    public SqliteProgressRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public ExerciseProgress exercise(ExerciseKey key) {
        return query("SELECT " + COLUMNS + " FROM exercise_progress WHERE subject = ? AND exercise = ?",
                statement -> {
                    statement.setString(1, key.subject());
                    statement.setString(2, key.exercise());
                    try (ResultSet rows = statement.executeQuery()) {
                        return rows.next() ? progress(rows) : ExerciseProgress.NONE;
                    }
                });
    }

    @Override
    public Map<String, ExerciseProgress> exercises(String subject) {
        return query("SELECT " + COLUMNS + " FROM exercise_progress WHERE subject = ?", statement -> {
            statement.setString(1, subject);
            Map<String, ExerciseProgress> found = new HashMap<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.put(rows.getString("exercise"), progress(rows));
                }
            }
            return found;
        });
    }

    @Override
    public void saveExercise(ExerciseKey key, ExerciseProgress progress, Instant at) {
        boolean newAttempt = progress.attempts() > exercise(key).attempts();
        query("""
                INSERT INTO exercise_progress (subject, exercise, attempts, last_verdict, solved, hints_revealed, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (subject, exercise) DO UPDATE SET
                    attempts = excluded.attempts, last_verdict = excluded.last_verdict, solved = excluded.solved,
                    hints_revealed = excluded.hints_revealed, updated_at = excluded.updated_at
                """, statement -> {
            statement.setString(1, key.subject());
            statement.setString(2, key.exercise());
            statement.setInt(3, progress.attempts());
            statement.setString(4, progress.lastVerdict().map(Verdict::name).orElse(null));
            statement.setInt(5, progress.solved() ? 1 : 0);
            statement.setInt(6, progress.hintsRevealed());
            statement.setString(7, at.toString());
            return statement.executeUpdate();
        });
        if (newAttempt && progress.lastVerdict().isPresent()) {
            query("INSERT INTO attempt (subject, exercise, at, verdict) VALUES (?, ?, ?, ?)", statement -> {
                statement.setString(1, key.subject());
                statement.setString(2, key.exercise());
                statement.setString(3, at.toString());
                statement.setString(4, progress.lastVerdict().get().name());
                return statement.executeUpdate();
            });
        }
    }

    @Override
    public void saveSessionFinished(SessionKey key, Instant at, Duration spent) {
        query("INSERT INTO session_finished (subject, session, at, spent_seconds) VALUES (?, ?, ?, ?)", statement -> {
            statement.setString(1, key.subject());
            statement.setString(2, key.session());
            statement.setString(3, at.toString());
            statement.setLong(4, spent.toSeconds());
            return statement.executeUpdate();
        });
    }

    @Override
    public Set<String> finishedSessions(String subject) {
        return query("SELECT DISTINCT session FROM session_finished WHERE subject = ?", statement -> {
            statement.setString(1, subject);
            Set<String> found = new HashSet<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(rows.getString(1));
                }
            }
            return found;
        });
    }

    private static ExerciseProgress progress(ResultSet row) throws SQLException {
        return new ExerciseProgress(
                row.getInt("attempts"),
                Optional.ofNullable(row.getString("last_verdict")).map(Verdict::valueOf),
                row.getInt("solved") == 1,
                row.getInt("hints_revealed"));
    }

    private <T> T query(String sql, StatementWork<T> work) {
        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            return work.run(statement);
        } catch (SQLException e) {
            throw new IllegalStateException("progress database: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface StatementWork<T> {
        T run(PreparedStatement statement) throws SQLException;
    }
}
