package io.github.lucasssoh.boussole.server.files;

import io.github.lucasssoh.boussole.core.learn.ContentNotFoundException;
import io.github.lucasssoh.boussole.core.learn.OpenCourse;
import io.github.lucasssoh.boussole.core.reference.ShowDocument;
import java.io.IOException;
import java.nio.file.Files;
import io.github.lucasssoh.boussole.format.model.Course;
import io.github.lucasssoh.boussole.server.block.SourcePaths;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The files of a subject folder: the learner's documents and the images of the sessions. A Word or
 * PowerPoint document comes as its PDF, which the browser shows, unless {@code ?original} asks for
 * the file itself. Nothing outside the folder, and no hidden file, is served.
 */
@RestController
class SubjectFileController {

    private final OpenCourse openCourse;
    private final ShowDocument showDocument;

    SubjectFileController(OpenCourse openCourse, ShowDocument showDocument) {
        this.openCourse = openCourse;
        this.showDocument = showDocument;
    }

    @GetMapping("/api/subjects/{subject}/files/{*path}")
    ResponseEntity<Resource> file(@PathVariable String subject, @PathVariable String path,
                                  @RequestParam(required = false) String original) throws IOException {
        return file(subject, Optional.empty(), path, original != null);
    }

    /**
     * A file of one version of the track ({@code lang}), each with its own folder. The language is
     * in the path, so that the relative links and images of a session resolve against its folder.
     */
    @GetMapping("/api/subjects/{subject}/lang/{lang}/files/{*path}")
    ResponseEntity<Resource> file(@PathVariable String subject, @PathVariable String lang, @PathVariable String path,
                                  @RequestParam(required = false) String original) throws IOException {
        return file(subject, Optional.of(lang), path, original != null);
    }

    /**
     * A file of the subject folder, or of its sources folder when it lies elsewhere: a version of a
     * track in another language may read the documents of the original ({@code sources: ../python/sources}).
     */
    private ResponseEntity<Resource> file(String subject, Optional<String> lang, String path, boolean original) throws IOException {
        Course course = openCourse.open(subject, lang).course();
        Path root = course.root().toRealPath();
        Path relative = Path.of(path.replaceFirst("^/+", "")).normalize();
        if (relative.isAbsolute() || isHidden(relative)) {
            throw new ContentNotFoundException("no file " + path + " in " + subject);
        }
        Path sources = root.resolve(course.subject().sourcesDir()).normalize();
        Path file = relative.startsWith(SourcePaths.SOURCES) && relative.getNameCount() > 1
                ? sources.resolve(relative.subpath(1, relative.getNameCount())).normalize()
                : root.resolve(relative).normalize();
        if (!Files.isRegularFile(file) || !within(file.toRealPath(), root, sources)) {
            throw new ContentNotFoundException("no file " + path + " in " + subject);
        }
        Path shown = original ? file : showDocument.fileToShow(file)
                .orElseThrow(() -> new ContentNotFoundException("no PDF of " + path + ": LibreOffice is needed to show it"));
        Resource resource = new FileSystemResource(shown);
        MediaType type = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).body(resource);
    }

    private static boolean within(Path file, Path root, Path sources) throws IOException {
        return file.startsWith(root) || (Files.isDirectory(sources) && file.startsWith(sources.toRealPath()));
    }

    private static boolean isHidden(Path relative) {
        return StreamSupport.stream(relative.spliterator(), false).anyMatch(part -> part.toString().startsWith("."));
    }
}
