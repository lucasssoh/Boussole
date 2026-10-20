package io.github.lucasssoh.boussole.server.files;

import io.github.lucasssoh.boussole.core.learn.ContentNotFoundException;
import io.github.lucasssoh.boussole.core.learn.OpenCourse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * The files of a subject folder: the learner's documents (PDFs) and the images of the sessions.
 * Nothing outside the folder, and no hidden file, is served.
 */
@RestController
class SubjectFileController {

    private final OpenCourse openCourse;

    SubjectFileController(OpenCourse openCourse) {
        this.openCourse = openCourse;
    }

    @GetMapping("/api/subjects/{subject}/files/{*path}")
    ResponseEntity<Resource> file(@PathVariable String subject, @PathVariable String path) throws IOException {
        Path root = openCourse.open(subject).course().root().toRealPath();
        Path relative = Path.of(path.replaceFirst("^/+", "")).normalize();
        if (relative.isAbsolute() || relative.startsWith("..") || isHidden(relative)) {
            throw new ContentNotFoundException("no file " + path + " in " + subject);
        }
        Path file = root.resolve(relative);
        if (!Files.isRegularFile(file) || !file.toRealPath().startsWith(root)) {
            throw new ContentNotFoundException("no file " + path + " in " + subject);
        }
        Resource resource = new FileSystemResource(file);
        MediaType type = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).body(resource);
    }

    private static boolean isHidden(Path relative) {
        return StreamSupport.stream(relative.spliterator(), false).anyMatch(part -> part.toString().startsWith("."));
    }
}
