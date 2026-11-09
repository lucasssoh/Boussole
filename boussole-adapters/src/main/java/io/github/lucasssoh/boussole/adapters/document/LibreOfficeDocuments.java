package io.github.lucasssoh.boussole.adapters.document;

import io.github.lucasssoh.boussole.core.reference.OfficeDocuments;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Makes PDFs with LibreOffice ({@code soffice --headless --convert-to pdf}), when it is installed,
 * and keeps them in a folder of Boussole: a document is converted again only once it changes. The
 * learner's folder is never written to. Each conversion runs with a profile of its own, so that it
 * neither waits for nor disturbs a LibreOffice the learner has open.
 */
public final class LibreOfficeDocuments implements OfficeDocuments {

    private static final long TIMEOUT_SECONDS = 120;

    private final Path cache;
    private final Optional<Path> soffice;
    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public LibreOfficeDocuments(Path cache) {
        this(cache, find());
    }

    LibreOfficeDocuments(Path cache, Optional<Path> soffice) {
        this.cache = cache;
        this.soffice = soffice;
    }

    /** Whether LibreOffice is installed where Boussole looks for it. */
    public static boolean installed() {
        return find().isPresent();
    }

    @Override
    public Optional<Path> pdfOf(Path document) {
        if (soffice.isEmpty() || !Files.isRegularFile(document)) {
            return Optional.empty();
        }
        try {
            String key = key(document);
            Path pdf = cache.resolve(key + ".pdf");
            synchronized (locks.computeIfAbsent(key, k -> new Object())) {
                if (Files.isRegularFile(pdf)) {
                    return Optional.of(pdf);
                }
                return convert(document, pdf);
            }
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Optional<Path> convert(Path document, Path pdf) throws IOException, InterruptedException {
        Files.createDirectories(cache);
        Path work = Files.createTempDirectory(cache, "convert-");
        try {
            Process process = new ProcessBuilder(soffice.get().toString(),
                    "-env:UserInstallation=" + work.resolve("profile").toUri(),
                    "--headless", "--norestore", "--convert-to", "pdf", "--outdir", work.toString(), document.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }
            String name = document.getFileName().toString();
            int dot = name.lastIndexOf('.');
            Path made = work.resolve((dot > 0 ? name.substring(0, dot) : name) + ".pdf");
            if (process.exitValue() != 0 || !Files.isRegularFile(made)) {
                return Optional.empty();
            }
            Files.move(made, pdf, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return Optional.of(pdf);
        } finally {
            deleteAll(work);
        }
    }

    /** The document, its size and the moment it last changed: a document that changes gets a new PDF. */
    private static String key(Path document) throws IOException {
        String identity = document.toRealPath() + "\n" + Files.size(document) + "\n" + Files.getLastModifiedTime(document).toMillis();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteAll(Path folder) throws IOException {
        try (Stream<Path> files = Files.walk(folder)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    /** {@code soffice} or {@code libreoffice} on the PATH, else where the installers of macOS and Windows put it. */
    private static Optional<Path> find() {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        List<String> names = windows ? List.of("soffice.exe", "soffice.com") : List.of("soffice", "libreoffice");
        String path = Optional.ofNullable(System.getenv("PATH")).orElse("");
        Stream<Path> onPath = Stream.of(path.split(File.pathSeparator)).filter(dir -> !dir.isBlank())
                .flatMap(dir -> names.stream().map(name -> Path.of(dir, name)));
        Stream<Path> installed = Stream.of(
                Path.of("/Applications/LibreOffice.app/Contents/MacOS/soffice"),
                Path.of("C:\\Program Files\\LibreOffice\\program\\soffice.exe"),
                Path.of("C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe"));
        return Stream.concat(onPath, installed).filter(Files::isExecutable).findFirst();
    }
}
