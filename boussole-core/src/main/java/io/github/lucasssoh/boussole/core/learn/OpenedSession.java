package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.core.learn.progress.ExerciseProgress;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.format.model.CourseSession;
import io.github.lucasssoh.boussole.format.model.block.Block;
import java.util.Map;

/**
 * A session ready to be studied: its content, the exercises it asks again with {@code ::ref}
 * (by ID), and the learner's progress on the exercises of the subject (by ID).
 */
public record OpenedSession(
        Course course, CourseSession session, Map<String, Block> referenced, Map<String, ExerciseProgress> progress) {

    public OpenedSession {
        referenced = Map.copyOf(referenced);
        progress = Map.copyOf(progress);
    }
}
