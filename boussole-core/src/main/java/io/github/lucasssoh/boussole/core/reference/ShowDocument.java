package io.github.lucasssoh.boussole.core.reference;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * The file the viewer shows for a document of the sources. A browser shows a PDF but not a Word
 * or PowerPoint file, which it would only download: such a document is shown as its PDF, so that
 * a course may cite its pages like those of any PDF.
 */
public final class ShowDocument {

    /** The documents of an office suite that are shown as a PDF. */
    private static final Set<String> CONVERTED = Set.of("doc", "docx", "odt", "ppt", "pptx", "odp");

    private final OfficeDocuments office;

    public ShowDocument(OfficeDocuments office) {
        this.office = office;
    }

    /**
     * The file to show: the PDF of an office document, any other document as it is. Empty when an
     * office document could not be made into a PDF.
     */
    public Optional<Path> fileToShow(Path document) {
        String name = document.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0 || !CONVERTED.contains(name.substring(dot + 1))) {
            return Optional.of(document);
        }
        return office.pdfOf(document);
    }
}
