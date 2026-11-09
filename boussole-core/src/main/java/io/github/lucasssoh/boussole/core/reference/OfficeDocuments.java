package io.github.lucasssoh.boussole.core.reference;

import java.nio.file.Path;
import java.util.Optional;

/**
 * The documents of an office suite (Word, Writer, PowerPoint, Impress), as far as Boussole needs
 * them (port): a PDF that a browser can show.
 */
public interface OfficeDocuments {

    /**
     * The PDF of this document, made once and kept while the document does not change. Never
     * throws: empty when no converter is installed or the conversion failed.
     */
    Optional<Path> pdfOf(Path document);
}
