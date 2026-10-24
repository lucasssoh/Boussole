package io.github.lucasssoh.boussole.format.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * How the chapters of a course follow one another: a chapter waits for every chapter of the same
 * course that one of its sessions has a prerequisite in, so that a chapter is finished before the
 * chapters that need it. {@code after} gives, for each chapter in the order of the syllabus, the
 * chapters it waits for.
 */
public record ChapterLinks(Map<String, Set<String>> after) {

    public ChapterLinks {
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        after.forEach((chapter, before) -> copy.put(chapter, Set.copyOf(before)));
        after = Collections.unmodifiableMap(copy);
    }

    public static ChapterLinks of(Course course) {
        String subject = course.subject().id();
        Map<String, Set<String>> after = new LinkedHashMap<>();
        course.syllabus().chapters().forEach(chapter -> after.put(chapter.id(), new LinkedHashSet<>()));
        for (CourseSession session : course.sessions()) {
            for (String prerequisite : session.session().header().prerequisites()) {
                String[] parts = prerequisite.split("\\.");
                if (parts.length == 3 && parts[0].equals(subject) && after.containsKey(parts[1])
                        && !parts[1].equals(session.chapter().id())) {
                    after.get(session.chapter().id()).add(parts[1]);
                }
            }
        }
        return new ChapterLinks(after);
    }

    /** The chapters this one waits for. */
    public Set<String> after(String chapter) {
        return after.getOrDefault(chapter, Set.of());
    }

    /** The links once those another path already gives are left out: what a map of the chapters draws. */
    public Map<String, Set<String>> direct() {
        Map<String, Set<String>> direct = new LinkedHashMap<>();
        after.forEach((chapter, before) -> direct.put(chapter, before.stream()
                .filter(one -> before.stream().noneMatch(other -> !other.equals(one) && reaches(other, one, new HashSet<>())))
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        return direct;
    }

    /** A loop of chapters waiting for one another, if there is one, from its first chapter back to it. */
    public Optional<List<String>> loop() {
        Map<String, Integer> state = new LinkedHashMap<>();
        for (String chapter : after.keySet()) {
            List<String> path = new ArrayList<>();
            Optional<List<String>> found = loopFrom(chapter, state, path);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    /** Whether {@code from} waits, directly or not, for {@code to}. */
    private boolean reaches(String from, String to, Set<String> seen) {
        if (!seen.add(from)) {
            return false;
        }
        for (String next : after(from)) {
            if (next.equals(to) || reaches(next, to, seen)) {
                return true;
            }
        }
        return false;
    }

    /** Depth first: 1 while on the path, 2 once done; a chapter met again on the path closes a loop. */
    private Optional<List<String>> loopFrom(String chapter, Map<String, Integer> state, List<String> path) {
        Integer seen = state.get(chapter);
        if (seen != null && seen == 2) {
            return Optional.empty();
        }
        if (seen != null) {
            List<String> loop = new ArrayList<>(path.subList(path.indexOf(chapter), path.size()));
            loop.add(chapter);
            return Optional.of(loop);
        }
        state.put(chapter, 1);
        path.add(chapter);
        for (String next : after(chapter)) {
            Optional<List<String>> found = loopFrom(next, state, path);
            if (found.isPresent()) {
                return found;
            }
        }
        path.removeLast();
        state.put(chapter, 2);
        return Optional.empty();
    }
}
