package io.github.lucasssoh.boussole.adapters.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LibreOfficeDocumentsTest {

    @TempDir
    Path folder;

    @Test
    void without_libreoffice_there_is_no_pdf() throws Exception {
        Path document = Files.writeString(folder.resolve("td.doc"), "not converted");

        assertThat(new LibreOfficeDocuments(folder.resolve("cache"), Optional.empty()).pdfOf(document)).isEmpty();
    }

    @Test
    void a_document_becomes_a_pdf_once_and_again_when_it_changes() throws Exception {
        assumeTrue(LibreOfficeDocuments.installed(), "LibreOffice is not installed");
        Path document = writerDocument("Le pattern Composite");
        Path cache = folder.resolve("cache");
        LibreOfficeDocuments office = new LibreOfficeDocuments(cache);

        Path pdf = office.pdfOf(document).orElseThrow();
        assertThat(pdf).startsWith(cache);
        assertThat(Files.readString(pdf, java.nio.charset.StandardCharsets.ISO_8859_1)).startsWith("%PDF");
        FileTime made = Files.getLastModifiedTime(pdf);
        assertThat(office.pdfOf(document)).contains(pdf);
        assertThat(Files.getLastModifiedTime(pdf)).isEqualTo(made);

        Files.setLastModifiedTime(document, FileTime.from(Instant.now().plusSeconds(60)));
        assertThat(office.pdfOf(document).orElseThrow()).isNotEqualTo(pdf);
        assertThat(Files.list(cache).filter(file -> file.getFileName().toString().startsWith("convert-"))).isEmpty();
    }

    /** A Writer document, made by LibreOffice itself from a text file. */
    private Path writerDocument(String text) throws Exception {
        Path source = Files.writeString(folder.resolve("composite.txt"), text);
        Process process = new ProcessBuilder("soffice", "-env:UserInstallation=" + folder.resolve("profile").toUri(),
                "--headless", "--convert-to", "odt", "--outdir", folder.toString(), source.toString())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        assertThat(process.waitFor(120, TimeUnit.SECONDS)).isTrue();
        Path document = folder.resolve("composite.odt");
        assumeTrue(Files.isRegularFile(document), "LibreOffice could not make the test document");
        return document;
    }
}
