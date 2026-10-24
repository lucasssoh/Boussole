package io.github.lucasssoh.boussole.core.learn.reward;

import io.github.lucasssoh.boussole.core.learn.CourseQuestions;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.TrackLevel;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What a track, or one of its sessions, lets the learner earn: every occasion a written session
 * gives to answer a question, its own questions whole, the ones it asks again ({@code ::ref}) for
 * {@link PointRules#AGAIN}, times the level of the track; what the first answer given there earned,
 * and what it missed. The bonus of a session without a mistake comes on top.
 */
public final class TrackScores {

    /** One occasion: a question in a session, what it may earn there, and its first answer there if any. */
    record Occasion(String session, String exercise, double possible, Optional<Double> earned) {
    }

    private TrackScores() {
    }

    public static TrackScore of(Course course, List<AttemptRecord> attempts) {
        return sum(occasions(course, attempts));
    }

    public static TrackScore ofSession(Course course, String session, List<AttemptRecord> attempts) {
        return sum(occasions(course, attempts).stream().filter(occasion -> occasion.session().equals(session)).toList());
    }

    private static TrackScore sum(List<Occasion> occasions) {
        double possible = 0;
        double earned = 0;
        double missed = 0;
        for (Occasion occasion : occasions) {
            possible += occasion.possible();
            if (occasion.earned().isPresent()) {
                earned += occasion.earned().get();
                missed += Math.max(0, occasion.possible() - occasion.earned().get());
            }
        }
        return new TrackScore(earned, missed, possible);
    }

    static List<Occasion> occasions(Course course, List<AttemptRecord> attempts) {
        String subject = course.subject().id();
        double coefficient = course.subject().level().map(TrackLevel::coefficient).orElse(1.0);
        Map<String, Double> firstHere = new HashMap<>();
        for (AttemptRecord attempt : attempts) {
            if (attempt.subject().equals(subject) && attempt.session().isPresent()) {
                firstHere.putIfAbsent(attempt.session().get() + "/" + attempt.exercise(), attempt.points());
            }
        }
        List<Occasion> occasions = new ArrayList<>();
        for (CourseSession session : course.sessions()) {
            String id = session.entry().id();
            Map<String, Double> here = new LinkedHashMap<>();
            session.session().blocks().forEach(block -> {
                if (block instanceof ExerciseRefBlock ref) {
                    CourseQuestions.find(course, ref.id())
                            .ifPresent(question -> here.putIfAbsent(ref.id(), PointRules.AGAIN * QuestionScore.possible(question)));
                } else {
                    CourseQuestions.id(block).ifPresent(question -> here.put(question, QuestionScore.possible(block)));
                }
            });
            here.forEach((exercise, possible) -> {
                if (possible > 0) {
                    occasions.add(new Occasion(id, exercise, possible * coefficient, Optional.ofNullable(firstHere.get(id + "/" + exercise))));
                }
            });
        }
        return occasions;
    }
}
