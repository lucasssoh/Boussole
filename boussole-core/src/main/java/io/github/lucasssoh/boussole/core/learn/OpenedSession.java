package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.check.Verdict;
import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.Map;

/**
 * A session ready to be studied: its content, the exercises it asks again with {@code ::ref}
 * (by ID), the learner's progress on the exercises of the subject (by ID), and in a lesson, the
 * questions already answered in it with the verdict of that answer, which is final.
 */
public record OpenedSession(
        Course course,
        CourseSession session,
        Map<String, Block> referenced,
        Map<String, ExerciseProgress> progress,
        Map<String, Verdict> answered) {

    public OpenedSession {
        referenced = Map.copyOf(referenced);
        progress = Map.copyOf(progress);
        answered = Map.copyOf(answered);
    }
}
