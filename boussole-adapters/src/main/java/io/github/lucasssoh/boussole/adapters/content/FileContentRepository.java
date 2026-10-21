package io.github.lucasssoh.boussole.adapters.content;

import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Reads the courses from their subject folders. The list of folders is asked for each time, so a
 * subject added to the configuration shows at once; a folder is read again only when one of its
 * course files changed, so the interface always shows what the agent just wrote.
 */
public final class FileContentRepository implements ContentRepository {

    private final Supplier<List<Path>> folders;
    private final Map<Path, SubjectFolderCache> caches = new ConcurrentHashMap<>();

    public FileContentRepository(Supplier<List<Path>> folders) {
        this.folders = folders;
    }

    /** A second folder with the same ID and the same language as an earlier one is left out. */
    @Override
    public List<Course> courses() {
        Set<String> seen = new HashSet<>();
        return folders.get().stream()
                .flatMap(folder -> caches.computeIfAbsent(folder, SubjectFolderCache::new).course().stream())
                .filter(course -> seen.add(course.subject().id() + "\u0000" + course.subject().lang()))
                .toList();
    }
}
