package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.cli.init.InitCommand;
import io.github.lucasssoh.boussole.cli.init.NewCommand;
import io.github.lucasssoh.boussole.cli.serve.OpenCommand;
import io.github.lucasssoh.boussole.cli.serve.ServeCommand;
import io.github.lucasssoh.boussole.cli.serve.StopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** The {@code boussole} command. Each subcommand is its own class. */
@Command(
        name = "boussole",
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersion.class,
        description = "Turn your course documents into a course, and plan when to study it.",
        subcommands = {OpenCommand.class, ServeCommand.class, StopCommand.class, NewCommand.class, InitCommand.class, ValidateCommand.class, SourcesCommand.class})
public final class BoussoleCommand {

    public static void main(String[] args) {
        int status = new CommandLine(new BoussoleCommand()).execute(args);
        if (!isServe(args)) {
            System.exit(status);
        }
    }

    /** {@code serve} returns once the server is up; the server then keeps the program running. */
    private static boolean isServe(String[] args) {
        return args.length > 0 && args[0].equals("serve");
    }
}
