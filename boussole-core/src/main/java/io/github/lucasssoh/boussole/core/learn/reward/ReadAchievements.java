package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.core.learn.CourseQuestions;
import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.SessionKind;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
import io.github.lucasssoh.boussole.format.model.answer.FillAnswer;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Where the learner stands on each {@link Achievement}, counted from the history of the subjects
 * that are rewarded; the guide counts for {@link Achievement#COMPASS_IN_HAND} alone. A track is
 * finished when every session of its plan is written and finished.
 */
public final class ReadAchievements {

    /** The guide a new Boussole opens on. */
    public static final String GUIDE = "boussole-guide";

    private final ContentRepository content;
    private final ProgressRepository progress;
    private final RewardedSubjects rewarded;
    private final ReadStudyStats stats;

    public ReadAchievements(ContentRepository content, ProgressRepository progress, RewardedSubjects rewarded, Clock clock) {
        this.content = content;
        this.progress = progress;
        this.rewarded = rewarded;
        this.stats = new ReadStudyStats(content, progress, rewarded, clock);
    }

    public List<AchievementProgress> read() {
        Records records = stats.read().records();
        List<AttemptRecord> attempts = progress.attemptHistory().stream().filter(attempt -> rewarded.rewards(attempt.subject())).toList();
        List<SessionFinishedRecord> finished = progress.sessionsFinished();
        Map<String, Course> courses = new LinkedHashMap<>();
        content.courses().forEach(course -> courses.putIfAbsent(course.subject().id(), course));
        Map<String, Instant> completed = completed(courses, finished);

        Map<Achievement, Integer> values = new LinkedHashMap<>();
        values.put(Achievement.COMPASS_IN_HAND, completed.containsKey(GUIDE) ? 1 : 0);
        values.put(Achievement.THE_FIRST_STEP, Math.min(1, records.sessionsFinished()));
        values.put(Achievement.DROP_BY_DROP, records.bestStreak());
        values.put(Achievement.THE_PATH_IS_MADE_BY_WALKING, records.sessionsFinished());
        values.put(Achievement.WITHOUT_A_BLOT, records.flawlessSessions());
        values.put(Achievement.AT_THE_FIRST_STROKE, records.firstTryRight());
        values.put(Achievement.THE_RIGHT_WORD, rightQuestions(courses, attempts, block -> block instanceof ExerciseBlock exercise
                && exercise.answer().orElse(null) instanceof FillAnswer));
        values.put(Achievement.NOTHING_ESCAPES_ME, rightQuestions(courses, attempts, block -> block instanceof McqBlock mcq && mcq.multiple()));
        values.put(Achievement.ALONE_AT_THE_HELM, withoutNudge(courses, finished));
        values.put(Achievement.ARIADNES_THREAD, rightAgain(attempts, false));
        values.put(Achievement.TWENTY_TIMES_ON_THE_LOOM, rightAgain(attempts, true));
        values.put(Achievement.THE_MADELEINE, (int) finished.stream()
                .filter(session -> rewarded.rewards(session.subject()) && session.bonus() > 0)
                .filter(session -> kindOf(courses, session.subject(), session.session()) == SessionKind.REVIEW)
                .count());
        List<Course> done = completed.keySet().stream().filter(rewarded::rewards).map(courses::get).toList();
        values.put(Achievement.ITHACA, done.size());
        values.put(Achievement.THE_GOLDEN_FLEECE, (int) done.stream().filter(course -> {
            TrackScore score = TrackScores.of(course, attempts);
            return score.possible() > 0 && score.earned() >= 0.9 * score.possible();
        }).count());
        values.put(Achievement.TRIAL_BY_FIRE, bestExam(courses, finished, attempts));
        values.put(Achievement.THE_HONEST_MAN, (int) done.stream().map(course -> course.subject().domain()).flatMap(Optional::stream).distinct().count());
        values.put(Achievement.ON_THE_SHOULDERS_OF_GIANTS, finishedAt(done, TrackLevel.MASTER));
        values.put(Achievement.TERRA_INCOGNITA, finishedAt(done, TrackLevel.RESEARCH));
        values.put(Achievement.FROM_STREAM_TO_OCEAN, streamToOcean(done, completed) ? 1 : 0);
        return values.entrySet().stream().map(entry -> AchievementProgress.of(entry.getKey(), entry.getValue())).toList();
    }

    /** The tracks whose every planned session is written and finished, with when the last one was. */
    private static Map<String, Instant> completed(Map<String, Course> courses, List<SessionFinishedRecord> finished) {
        Map<String, Map<String, Instant>> lastFinish = new HashMap<>();
        finished.forEach(session -> lastFinish.computeIfAbsent(session.subject(), unused -> new HashMap<>()).merge(session.session(), session.at(),
                (a, b) -> a.isBefore(b) ? a : b));
        Map<String, Instant> completed = new HashMap<>();
        courses.forEach((subject, course) -> {
            List<String> planned = course.syllabus().chapters().stream().flatMap(chapter -> chapter.sessions().stream()).map(entry -> entry.id()).toList();
            Map<String, Instant> done = lastFinish.getOrDefault(subject, Map.of());
            if (!planned.isEmpty() && course.sessions().size() == planned.size() && done.keySet().containsAll(planned)) {
                completed.put(subject, planned.stream().map(done::get).max(Instant::compareTo).orElseThrow());
            }
        });
        return completed;
    }

    private static int rightQuestions(Map<String, Course> courses, List<AttemptRecord> attempts, Predicate<Block> kind) {
        return (int) attempts.stream()
                .filter(attempt -> attempt.verdict() == Verdict.CORRECT)
                .filter(attempt -> Optional.ofNullable(courses.get(attempt.subject()))
                        .flatMap(course -> CourseQuestions.find(course, attempt.exercise())).filter(kind).isPresent())
                .map(attempt -> attempt.subject() + "/" + attempt.exercise())
                .distinct()
                .count();
    }

    /** Finished sessions whose questions the learner answered without revealing a single nudge. */
    private int withoutNudge(Map<String, Course> courses, List<SessionFinishedRecord> finished) {
        int count = 0;
        Set<String> seen = new HashSet<>();
        for (SessionFinishedRecord session : finished) {
            Course course = courses.get(session.subject());
            if (course == null || !rewarded.rewards(session.subject()) || !seen.add(session.subject() + "/" + session.session())) {
                continue;
            }
            Map<String, ExerciseProgress> exercises = progress.exercises(session.subject());
            List<String> questions = questionsOf(course, session.session());
            boolean answered = questions.stream().anyMatch(id -> exercises.getOrDefault(id, ExerciseProgress.NONE).attempts() > 0);
            boolean noNudge = questions.stream().allMatch(id -> exercises.getOrDefault(id, ExerciseProgress.NONE).hintsRevealed() == 0);
            if (answered && noNudge) {
                count++;
            }
        }
        return count;
    }

    /**
     * Questions right when asked again in another session: all of them, or ({@code missedFirst})
     * only those whose first answer was not right.
     */
    private static int rightAgain(List<AttemptRecord> attempts, boolean missedFirst) {
        Map<String, Verdict> first = new HashMap<>();
        Set<String> occasions = new HashSet<>();
        Set<String> found = new HashSet<>();
        int right = 0;
        for (AttemptRecord attempt : attempts) {
            String question = attempt.subject() + "/" + attempt.exercise();
            boolean firstEver = !first.containsKey(question);
            first.putIfAbsent(question, attempt.verdict());
            boolean firstHere = attempt.session().map(session -> occasions.add(question + "@" + session)).orElse(false);
            if (!firstEver && firstHere && attempt.verdict() == Verdict.CORRECT) {
                if (!missedFirst) {
                    right++;
                } else if (first.get(question) != Verdict.CORRECT) {
                    found.add(question);
                }
            }
        }
        return missedFirst ? found.size() : right;
    }

    /** The best mock exam finished, in percent of the points it gives. */
    private int bestExam(Map<String, Course> courses, List<SessionFinishedRecord> finished, List<AttemptRecord> attempts) {
        return finished.stream()
                .filter(session -> rewarded.rewards(session.subject()) && kindOf(courses, session.subject(), session.session()) == SessionKind.EXAM)
                .mapToInt(session -> {
                    TrackScore score = TrackScores.ofSession(courses.get(session.subject()), session.session(), attempts);
                    return score.possible() > 0 ? (int) Math.floor(100 * score.earned() / score.possible()) : 0;
                })
                .max()
                .orElse(0);
    }

    private static int finishedAt(List<Course> done, TrackLevel level) {
        return (int) done.stream().filter(course -> course.subject().level().equals(Optional.of(level))).count();
    }

    private static boolean streamToOcean(List<Course> done, Map<String, Instant> completed) {
        List<Instant> discovery = new ArrayList<>();
        List<Instant> research = new ArrayList<>();
        for (Course course : done) {
            Instant at = completed.get(course.subject().id());
            course.subject().level().ifPresent(level -> {
                if (level == TrackLevel.DISCOVERY) {
                    discovery.add(at);
                } else if (level == TrackLevel.RESEARCH) {
                    research.add(at);
                }
            });
        }
        return discovery.stream().anyMatch(a -> research.stream().anyMatch(b -> Duration.between(a, b).abs().toDays() <= 365));
    }

    private static SessionKind kindOf(Map<String, Course> courses, String subject, String session) {
        return Optional.ofNullable(courses.get(subject))
                .flatMap(course -> course.sessions().stream().filter(found -> found.entry().id().equals(session)).findFirst())
                .map(found -> found.entry().kind())
                .orElse(SessionKind.LESSON);
    }

    private static List<String> questionsOf(Course course, String session) {
        return course.sessions().stream()
                .filter(found -> found.entry().id().equals(session))
                .findFirst()
                .map(CourseSession::session)
                .map(content -> content.blocks()
                        .flatMap(block -> block instanceof ExerciseRefBlock ref ? Stream.of(ref.id()) : CourseQuestions.id(block).stream())
                        .distinct()
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
