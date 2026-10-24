package io.github.lucasssoh.boussole.adapters.progress;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseKey;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.LastActivity;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.core.learn.reward.AttemptRecord;
import io.github.lucasssoh.boussole.core.learn.reward.SessionFinishedRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The learner's progress in SQLite, and the history of attempts, each with the session it was given in. */
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
    }

    @Override
    public void recordAttempt(ExerciseKey key, Optional<String> session, Verdict verdict, double points, Instant at) {
        query("INSERT INTO attempt (subject, exercise, session, at, verdict, points) VALUES (?, ?, ?, ?, ?, ?)", statement -> {
            statement.setString(1, key.subject());
            statement.setString(2, key.exercise());
            statement.setString(3, session.orElse(null));
            statement.setString(4, at.toString());
            statement.setString(5, verdict.name());
            statement.setDouble(6, points);
            return statement.executeUpdate();
        });
    }

    @Override
    public void saveSessionFinished(SessionKey key, Instant at, Duration spent, double bonus) {
        query("INSERT INTO session_finished (subject, session, at, spent_seconds, bonus) VALUES (?, ?, ?, ?, ?)", statement -> {
            statement.setString(1, key.subject());
            statement.setString(2, key.session());
            statement.setString(3, at.toString());
            statement.setLong(4, spent.toSeconds());
            statement.setDouble(5, bonus);
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

    @Override
    public Map<String, Verdict> firstVerdicts(SessionKey session) {
        return query("SELECT exercise, verdict FROM attempt WHERE subject = ? AND session = ? ORDER BY at, id", statement -> {
            statement.setString(1, session.subject());
            statement.setString(2, session.session());
            Map<String, Verdict> found = new HashMap<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.putIfAbsent(rows.getString("exercise"), Verdict.valueOf(rows.getString("verdict")));
                }
            }
            return found;
        });
    }

    @Override
    public List<AttemptRecord> attemptHistory() {
        return query("SELECT subject, exercise, session, at, verdict, points FROM attempt ORDER BY at, id", statement -> {
            List<AttemptRecord> found = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(new AttemptRecord(rows.getString("subject"), rows.getString("exercise"),
                            Optional.ofNullable(rows.getString("session")), Instant.parse(rows.getString("at")),
                            Verdict.valueOf(rows.getString("verdict")), rows.getDouble("points")));
                }
            }
            return found;
        });
    }

    @Override
    public List<SessionFinishedRecord> sessionsFinished() {
        return query("SELECT subject, session, at, bonus FROM session_finished ORDER BY at, id", statement -> {
            List<SessionFinishedRecord> found = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(new SessionFinishedRecord(rows.getString("subject"), rows.getString("session"),
                            Instant.parse(rows.getString("at")), rows.getDouble("bonus")));
                }
            }
            return found;
        });
    }

    @Override
    public Optional<LastActivity> lastActivity(String subject) {
        return query("""
                SELECT at, session, 0 AS finished FROM attempt WHERE subject = ?
                UNION ALL
                SELECT at, session, 1 FROM session_finished WHERE subject = ?
                ORDER BY at DESC LIMIT 1
                """, statement -> {
            statement.setString(1, subject);
            statement.setString(2, subject);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next()
                        ? Optional.of(new LastActivity(Instant.parse(rows.getString("at")),
                                Optional.ofNullable(rows.getString("session")), rows.getInt("finished") == 1))
                        : Optional.<LastActivity>empty();
            }
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
