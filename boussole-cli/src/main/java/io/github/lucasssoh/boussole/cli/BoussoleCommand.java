package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.cli.init.InitCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** The {@code boussole} command. Each subcommand is its own class. */
@Command(
        name = "boussole",
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersion.class,
        description = "Turn your course documents into a course, and plan when to study it.",
        subcommands = {InitCommand.class, ValidateCommand.class})
public final class BoussoleCommand {

    public static void main(String[] args) {
        System.exit(new CommandLine(new BoussoleCommand()).execute(args));
    }
}
