package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.util.List;

/**
 * Where courses are read from (port). A course read again after its files changed is up to date.
 * A track may have several versions, one per language: subjects with the same ID.
 */
public interface ContentRepository {

    /** Every version of every track, in the order of the configuration. */
    List<Course> courses();

    /** The versions of one track, in the order of the configuration; empty when there is none. */
    default List<Course> versions(String subject) {
        return courses().stream().filter(course -> course.subject().id().equals(subject)).toList();
    }
}
