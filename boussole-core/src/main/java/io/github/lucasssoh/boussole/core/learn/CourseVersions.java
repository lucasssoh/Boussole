package io.github.lucasssoh.boussole.core.learn;

import io.github.lucasssoh.boussole.format.model.Course;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The versions of one track, one per language: subjects with the same ID. The learner follows the
 * version in the language they asked for when there is one, else the first. Progress is kept by
 * ID, so it is the same in every language.
 */
final class CourseVersions {

    private CourseVersions() {
    }

    static Course pick(List<Course> versions, Optional<String> language) {
        return language.flatMap(wanted -> versions.stream().filter(course -> sameLanguage(course, wanted)).findFirst())
                .orElse(versions.getFirst());
    }

    static List<String> languages(List<Course> versions) {
        return versions.stream().map(course -> course.subject().lang()).distinct().toList();
    }

    private static boolean sameLanguage(Course course, String wanted) {
        String lang = course.subject().lang().toLowerCase(Locale.ROOT);
        String asked = wanted.toLowerCase(Locale.ROOT);
        return lang.equals(asked) || lang.startsWith(asked + "-") || asked.startsWith(lang + "-");
    }
}
