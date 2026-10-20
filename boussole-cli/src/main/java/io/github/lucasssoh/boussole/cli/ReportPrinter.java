package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.format.InspectionReport;
import io.github.lucasssoh.boussole.format.diagnostic.Diagnostic;
import io.github.lucasssoh.boussole.format.model.Course;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Prints an inspection: one line per diagnostic, {@code path:line: error F020 message}, with
 * paths as typed so terminals and editors can open them, then a summary line.
 */
final class ReportPrinter {

    private final PrintWriter out;
    private final Path folder;
    private final boolean errorsOnly;

    ReportPrinter(PrintWriter out, Path folder, boolean errorsOnly) {
        this.out = out;
        this.folder = folder;
        this.errorsOnly = errorsOnly;
    }

    void print(InspectionReport report) {
        report.diagnostics().stream()
                .filter(diagnostic -> diagnostic.isError() || !errorsOnly)
                .map(this::line)
                .forEach(out::println);
        out.println(summary(report));
        out.flush();
    }

    private String line(Diagnostic diagnostic) {
        String place = folder.resolve(diagnostic.position().file()).normalize().toString();
        if (diagnostic.position().line() > 0) {
            place += ":" + diagnostic.position().line();
        }
        return place + ": " + diagnostic.severity().name().toLowerCase(Locale.ROOT) + " " + diagnostic.code() + " "
                + diagnostic.message();
    }

    private String summary(InspectionReport report) {
        String sessions = report.course().map(ReportPrinter::sessions).orElse("nothing read");
        int errors = report.errors().size();
        int warnings = report.warnings().size();
        if (errors == 0 && warnings == 0) {
            return "✓ " + folder + ": no problem, " + sessions + ".";
        }
        return (errors == 0 ? "✓ " : "✗ ") + folder + ": " + count(errors, "error") + ", " + count(warnings, "warning")
                + "; " + sessions + ".";
    }

    private static String sessions(Course course) {
        long planned = course.syllabus().chapters().stream().mapToLong(chapter -> chapter.sessions().size()).sum();
        return course.sessions().size() + " of " + count((int) planned, "session") + " written";
    }

    private static String count(int number, String noun) {
        return number + " " + noun + (number == 1 ? "" : "s");
    }
}
