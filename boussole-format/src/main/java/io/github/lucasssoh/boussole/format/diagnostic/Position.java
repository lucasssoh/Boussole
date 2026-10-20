package io.github.lucasssoh.boussole.format.diagnostic;

/**
 * A place in a subject: a file relative to the subject folder, and a 1-based line, 0 when the
 * whole file is meant.
 */
public record Position(String file, int line) {

    public static Position fileOnly(String file) {
        return new Position(file, 0);
    }

    public Position atLine(int line) {
        return new Position(file, line);
    }

    @Override
    public String toString() {
        return line == 0 ? file : file + ":" + line;
    }
}
