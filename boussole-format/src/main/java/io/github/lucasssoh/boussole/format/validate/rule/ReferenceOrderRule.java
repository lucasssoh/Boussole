package io.github.lucasssoh.boussole.format.validate.rule;

import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCode;
import io.github.lucasssoh.boussole.format.diagnostic.DiagnosticCollector;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.ExerciseBlock;
import io.github.lucasssoh.boussole.format.model.block.ExerciseRefBlock;
import io.github.lucasssoh.boussole.format.model.block.McqBlock;
import io.github.lucasssoh.boussole.format.validate.ValidationContext;
import io.github.lucasssoh.boussole.format.validate.ValidationRule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * F042: a {@code ::ref} asks again a question of an earlier session, in the order of the syllabus:
 * a warmup or a review never asks about a notion the learner has not studied yet.
 */
public final class ReferenceOrderRule implements ValidationRule {

    @Override
    public void check(ValidationContext context, DiagnosticCollector diagnostics) {
        Course course = context.course();
        List<String> order = course.syllabus().chapters().stream()
                .flatMap(chapter -> chapter.sessions().stream())
                .map(entry -> entry.id())
                .toList();
        Map<String, String> sessionOf = new HashMap<>();
        for (CourseSession loaded : course.sessions()) {
            loaded.session().blocks().forEach(block -> {
                switch (block) {
                    case ExerciseBlock exercise -> sessionOf.put(exercise.id(), loaded.entry().id());
                    case McqBlock mcq -> sessionOf.put(mcq.id(), loaded.entry().id());
                    default -> { }
                }
            });
        }
        for (CourseSession loaded : course.sessions()) {
            String here = loaded.entry().id();
            loaded.session().blocks()
                    .filter(ExerciseRefBlock.class::isInstance)
                    .map(ExerciseRefBlock.class::cast)
                    .filter(ref -> sessionOf.containsKey(ref.id()))
                    .filter(ref -> order.indexOf(sessionOf.get(ref.id())) >= order.indexOf(here))
                    .forEach(ref -> diagnostics.report(DiagnosticCode.F042, ref.position(), "`::ref{id=" + ref.id()
                            + "}` asks a question of `" + sessionOf.get(ref.id()) + "`, which "
                            + (sessionOf.get(ref.id()).equals(here) ? "is this session" : "comes after `" + here + "`")
                            + ": ask again only questions of earlier sessions, or write a new question here"));
        }
    }
}
