package io.github.lucasssoh.boussole.adapters.content;

import io.github.lucasssoh.boussole.core.learn.ContentRepository;
import io.github.lucasssoh.boussole.format.model.Course;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public List<Course> courses() {
        return folders.get().stream()
                .flatMap(folder -> caches.computeIfAbsent(folder, SubjectFolderCache::new).course().stream())
                .toList();
    }

    @Override
    public Optional<Course> course(String subject) {
        return courses().stream().filter(course -> course.subject().id().equals(subject)).findFirst();
    }
}
