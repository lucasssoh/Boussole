package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.util.List;
import java.util.Optional;

/** Where courses are read from (port). A course read again after its files changed is up to date. */
public interface ContentRepository {

    List<Course> courses();

    Optional<Course> course(String subject);
}
