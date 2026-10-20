package io.github.lucasssoh.boussole.cli;

/** Exit statuses shared by the subcommands. */
final class ExitCode {

    static final int OK = 0;
    static final int PROBLEMS = 1;
    static final int UNUSABLE = 2;

    private ExitCode() {
    }
}
