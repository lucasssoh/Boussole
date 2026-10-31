package io.github.lucasssoh.boussole.cli;

import io.github.lucasssoh.boussole.adapters.config.InvalidConfigException;
import io.github.lucasssoh.boussole.cli.admin.AdminLauncher;
import io.github.lucasssoh.boussole.cli.devices.DevicesCommand;
import io.github.lucasssoh.boussole.cli.init.InitCommand;
import io.github.lucasssoh.boussole.cli.init.NewCommand;
import io.github.lucasssoh.boussole.cli.serve.OpenCommand;
import io.github.lucasssoh.boussole.cli.serve.ServeCommand;
import io.github.lucasssoh.boussole.cli.serve.StopCommand;
import java.io.IOException;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * The {@code boussole} command. Each subcommand is its own class; alone, in a terminal, it opens
 * the full screen of the server, the devices and the tracks (beta), else it prints its help.
 */
@Command(
        name = "boussole",
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersion.class,
        description = {"Turn your course documents into a course, and plan when to study it.",
            "Alone, in a terminal: the server, the devices and the tracks, full screen (beta)."},
        subcommands = {OpenCommand.class, ServeCommand.class, StopCommand.class, NewCommand.class, InitCommand.class, ValidateCommand.class, SourcesCommand.class, IndexCommand.class, DevicesCommand.class})
public final class BoussoleCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws IOException, InterruptedException {
        if (!AdminLauncher.hasTerminal()) {
            spec.commandLine().usage(spec.commandLine().getOut());
            return ExitCode.OK;
        }
        AdminLauncher.run();
        return ExitCode.OK;
    }

    public static void main(String[] args) {
        int status = new CommandLine(new BoussoleCommand()).setExecutionExceptionHandler(BoussoleCommand::failed).execute(args);
        if (!isServe(args)) {
            System.exit(status);
        }
    }

    /** A {@code config.yaml} that cannot be read is said in one line, where and why, not as a stack trace. */
    static int failed(Exception e, CommandLine command, CommandLine.ParseResult parsed) throws Exception {
        if (e instanceof InvalidConfigException invalid) {
            command.getErr().println(invalid.getMessage());
            return ExitCode.UNUSABLE;
        }
        throw e;
    }

    /** {@code serve} returns once the server is up; the server then keeps the program running. */
    private static boolean isServe(String[] args) {
        return args.length > 0 && args[0].equals("serve");
    }
}
