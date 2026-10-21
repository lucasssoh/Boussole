package io.github.lucasssoh.boussole.format.source;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * The documents of a folder. Names that lead outside the folder do not exist. When the sources
 * folder is the subject folder itself, the files Boussole and the agents write there (the
 * course, the subject, the agent kit) are not documents.
 */
public final class FolderSourceDocuments implements SourceDocuments {

    /** The kinds of files a teacher or a learner hands in: lecture notes, slides, sheets, scans, archives. */
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "pdf", "ppt", "pptx", "odp", "doc", "docx", "odt", "md", "txt", "tex", "zip", "png", "jpg", "jpeg",
            "mp4", "m4v", "webm", "ogv", "mov", "mkv", "avi");
    private static final Set<String> SUBJECT_FILES = Set.of("subject.yaml", "learner.yaml", "AGENTS.md", "CLAUDE.md");
    private static final String COURSE_DIR = "cursus";

    private final Path folder;
    private final boolean isSubjectFolder;
    private final Map<String, OptionalInt> pageCounts = new HashMap<>();
    private final Map<String, Optional<String>> digests = new HashMap<>();

    /** {@code folder}: the sources folder; {@code subjectFolder}: the subject folder holding it. */
    public FolderSourceDocuments(Path folder, Path subjectFolder) {
        this.folder = folder.toAbsolutePath().normalize();
        this.isSubjectFolder = this.folder.equals(subjectFolder.toAbsolutePath().normalize());
    }

    @Override
    public boolean exists(String file) {
        Path path = folder.resolve(file).normalize();
        return path.startsWith(folder) && Files.isRegularFile(path);
    }

    @Override
    public OptionalInt pageCount(String file) {
        if (!exists(file) || !file.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return OptionalInt.empty();
        }
        return pageCounts.computeIfAbsent(file, this::readPageCount);
    }

    @Override
    public List<String> documents() {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(folder)) {
            return files.filter(Files::isRegularFile)
                    .map(path -> folder.relativize(path).toString().replace('\\', '/'))
                    .filter(this::isDocument)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> sha256(String file) {
        if (!exists(file)) {
            return Optional.empty();
        }
        return digests.computeIfAbsent(file, this::readDigest);
    }

    private boolean isDocument(String relative) {
        if (Stream.of(relative.split("/")).anyMatch(part -> part.startsWith("."))) {
            return false;
        }
        if (isSubjectFolder && (SUBJECT_FILES.contains(relative) || relative.startsWith(COURSE_DIR + "/"))) {
            return false;
        }
        int dot = relative.lastIndexOf('.');
        return dot > 0 && DOCUMENT_TYPES.contains(relative.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private OptionalInt readPageCount(String file) {
        try (PDDocument document = Loader.loadPDF(folder.resolve(file).toFile())) {
            return OptionalInt.of(document.getNumberOfPages());
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    private Optional<String> readDigest(String file) {
        try (DigestInputStream in = new DigestInputStream(Files.newInputStream(folder.resolve(file)), MessageDigest.getInstance("SHA-256"))) {
            in.transferTo(OutputStream.nullOutputStream());
            return Optional.of(HexFormat.of().formatHex(in.getMessageDigest().digest()));
        } catch (IOException e) {
            return Optional.empty();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is part of every Java", e);
        }
    }
}
