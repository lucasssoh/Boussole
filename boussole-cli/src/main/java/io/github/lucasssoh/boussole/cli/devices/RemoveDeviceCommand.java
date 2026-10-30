package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole devices remove}: the device's key opens nothing any more. */
@Command(name = "remove", mixinStandardHelpOptions = true, description = "Remove a paired device. It can no longer open Boussole.")
final class RemoveDeviceCommand implements Callable<Integer> {

    @Parameters(paramLabel = "DEVICE", description = "Its name or its ID, as boussole devices lists them.")
    private String device;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            LocalServer server = LocalServer.ofConfig();
            JsonNode found = Devices.find(server, device);
            server.delete("/api/devices/" + found.path("id").asText());
            spec.commandLine().getOut().printf("Removed      %s  %s%n", found.path("name").asText(), found.path("address").asText());
            return ExitCode.OK;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }
}
