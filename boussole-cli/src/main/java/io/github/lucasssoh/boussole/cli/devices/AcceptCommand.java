package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole devices accept}: pairs the device that shows this code. */
@Command(name = "accept", mixinStandardHelpOptions = true, description = "Pair the device that shows this code.")
final class AcceptCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", paramLabel = "CODE", description = "The code the device shows, with or without spaces.")
    private List<String> code;

    @Option(names = "--read-only", description = "The device reads but cannot answer.")
    private boolean readOnly;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            JsonNode request = LocalServer.ofConfig().post("/api/devices/requests/" + String.join("", code) + "/accept",
                    Map.of("access", Devices.access(readOnly)));
            spec.commandLine().getOut().printf("Paired       %s  %s  %s%n", request.path("name").asText(), request.path("address").asText(),
                    Devices.access(readOnly));
            return ExitCode.OK;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }
}
