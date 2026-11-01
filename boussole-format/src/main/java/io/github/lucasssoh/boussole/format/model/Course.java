package io.github.lucasssoh.boussole.format.model;

import io.github.lucasssoh.boussole.format.model.block.Block;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A whole subject as read from its folder. The learner's profile is empty when the subject has
 * no {@code learner.yaml}: the course then follows the default teaching contract. The record of
 * its documents is empty until {@code cursus/sources.yaml} is written.
 */
public record Course(
        Path root,
        CourseSubject subject,
        CourseSyllabus syllabus,
        CourseLexicon lexicon,
        Optional<LearnerProfile> learner,
        Optional<CourseSources> sources,
        List<CourseSession> sessions) {

    public Stream<Block> blocks() {
        return sessions.stream().flatMap(loaded -> loaded.session().blocks());
    }

    /**
     * What a session needs first: the prerequisites of its front matter once written, those the
     * syllabus planned before that, or when the front matter has none.
     */
    public List<String> prerequisites(SyllabusChapter chapter, SyllabusSession entry) {
        return sessions.stream()
                .filter(session -> session.chapter().id().equals(chapter.id()) && session.entry().id().equals(entry.id()))
                .map(session -> session.session().header().prerequisites())
                .filter(written -> !written.isEmpty())
                .findFirst()
                .orElse(entry.prerequisites());
    }

    public SessionId sessionId(SyllabusChapter chapter, SyllabusSession entry) {
        return new SessionId(subject.id(), chapter.id(), entry.id());
    }
}
