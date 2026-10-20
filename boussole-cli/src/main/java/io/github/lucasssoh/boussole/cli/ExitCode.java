package io.github.lucasssoh.boussole.cli;

/** Exit statuses shared by the subcommands. */
public final class ExitCode {

    public static final int OK = 0;
    public static final int PROBLEMS = 1;
    public static final int UNUSABLE = 2;

    private ExitCode() {
    }
}
