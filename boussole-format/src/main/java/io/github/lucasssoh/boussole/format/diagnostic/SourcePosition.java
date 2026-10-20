package io.github.lucasssoh.boussole.format.diagnostic;

/**
 * A place in a subject: a file relative to the subject folder, and a 1-based line, 0 when the
 * whole file is meant.
 */
public record SourcePosition(String file, int line) {

    public static SourcePosition fileOnly(String file) {
        return new SourcePosition(file, 0);
    }

    public SourcePosition atLine(int line) {
        return new SourcePosition(file, line);
    }

    @Override
    public String toString() {
        return line == 0 ? file : file + ":" + line;
    }
}
