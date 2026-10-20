package io.github.lucasssoh.boussole.cli.init;

import io.github.lucasssoh.boussole.cli.init.InitReport.Action;

/**
 * Creates {@code subject.yaml} and an empty proposed syllabus, so that the folder is a valid
 * subject before any agent works on it. Never overwrites them.
 */
final class SubjectScaffold {

    static final String SUBJECT = "subject.yaml";
    static final String SYLLABUS = "cursus/syllabus.yaml";
    private static final String SOURCES = "sources";

    private final SubjectFolder folder;

    SubjectScaffold(SubjectFolder folder) {
        this.folder = folder;
    }

    /** Values given on the command line; empty ones are guessed from the folder. */
    record Settings(String id, String name, String lang, String sources) {
    }

    void create(Settings given, InitReport report) {
        if (folder.exists(SUBJECT)) {
            report.add(Action.KEPT, SUBJECT);
        } else {
            Settings settings = complete(given);
            folder.write(SUBJECT, subjectYaml(settings));
            report.add(Action.CREATED, SUBJECT + " (id " + settings.id() + ", lang " + settings.lang()
                    + ", documents in " + settings.sources() + ")");
        }
        if (folder.exists(SYLLABUS)) {
            report.add(Action.KEPT, SYLLABUS);
        } else {
            folder.write(SYLLABUS, "status: proposed\nchapters: []\n");
            report.add(Action.CREATED, SYLLABUS + " (proposed, no chapter yet)");
        }
    }

    /** Where the documents are: the given folder, else {@code sources/} if it exists, else the subject folder if it holds PDFs. */
    String sources(String given) {
        if (!given.isEmpty()) {
            return given;
        }
        if (folder.exists(SOURCES) || !folder.holdsPdfs()) {
            return SOURCES;
        }
        return ".";
    }

    private Settings complete(Settings given) {
        String folderName = folder.root().toAbsolutePath().normalize().getFileName().toString();
        return new Settings(
                given.id().isEmpty() ? Slug.of(folderName) : given.id(),
                given.name().isEmpty() ? folderName : given.name(),
                given.lang(),
                sources(given.sources()));
    }

    private static String subjectYaml(Settings settings) {
        return "format: 1\n"
                + "id: " + settings.id() + "\n"
                + "name: " + quoted(settings.name()) + "\n"
                + "lang: " + settings.lang() + "\n"
                + "sources: " + quoted(settings.sources()) + "\n";
    }

    private static String quoted(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
