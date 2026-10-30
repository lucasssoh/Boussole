package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole devices access}: full, or read-only. */
@Command(name = "access", mixinStandardHelpOptions = true, description = "Change what a paired device may do: full or read-only.")
final class AccessCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "DEVICE", description = "Its name or its ID.")
    private String device;

    @Parameters(index = "1", paramLabel = "ACCESS", description = "full or read-only.")
    private String access;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            LocalServer server = LocalServer.ofConfig();
            JsonNode edited = server.patch("/api/devices/" + Devices.find(server, device).path("id").asText(), Map.of("access", access));
            spec.commandLine().getOut().printf("Access       %s  %s%n", edited.path("name").asText(), edited.path("access").asText());
            return ExitCode.OK;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }
}
