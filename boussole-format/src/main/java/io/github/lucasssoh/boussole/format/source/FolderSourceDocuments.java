package io.github.lucasssoh.boussole.format.source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/** The documents of a folder. Names that lead outside the folder do not exist. */
public final class FolderSourceDocuments implements SourceDocuments {

    private final Path folder;
    private final Map<String, OptionalInt> pageCounts = new HashMap<>();

    public FolderSourceDocuments(Path folder) {
        this.folder = folder.toAbsolutePath().normalize();
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

    private OptionalInt readPageCount(String file) {
        try (PDDocument document = Loader.loadPDF(folder.resolve(file).toFile())) {
            return OptionalInt.of(document.getNumberOfPages());
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }
}
