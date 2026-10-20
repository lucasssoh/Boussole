package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ProgressRepository;
import io.github.lucasssoh.boussole.core.learn.progress.SessionKey;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Opens a written session, with the exercises it asks again and the learner's progress. */
public final class OpenSession {

    private final CourseLookup lookup;
    private final ProgressRepository progress;

    public OpenSession(ContentRepository content, ProgressRepository progress) {
        this.lookup = new CourseLookup(content);
        this.progress = progress;
    }

    public OpenedSession open(String subject, String session) {
        Course course = lookup.course(subject);
        CourseSession found = lookup.session(course, session);
        return new OpenedSession(course, found, referenced(course, found), progress.exercises(subject),
                AnswerOnce.appliesTo(found) ? progress.firstVerdicts(new SessionKey(subject, session)) : Map.of());
    }

    private static Map<String, Block> referenced(Course course, CourseSession session) {
        return session.session().blocks()
                .flatMap(block -> block instanceof ExerciseRefBlock ref ? Optional.of(ref.id()).stream() : Optional.<String>empty().stream())
                .distinct()
                .flatMap(id -> CourseQuestions.find(course, id).stream())
                .collect(Collectors.toMap(block -> CourseQuestions.id(block).orElseThrow(), Function.identity()));
    }
}
