package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** {@code boussole devices rename}: only the owner names devices. */
@Command(name = "rename", mixinStandardHelpOptions = true, description = "Rename a paired device.")
final class RenameDeviceCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "DEVICE", description = "Its name or its ID.")
    private String device;

    @Parameters(index = "1", paramLabel = "NAME", description = "Its new name.")
    private String name;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        try {
            LocalServer server = LocalServer.ofConfig();
            JsonNode edited = server.patch("/api/devices/" + Devices.find(server, device).path("id").asText(), Map.of("name", name));
            spec.commandLine().getOut().printf("Renamed      %s%n", edited.path("name").asText());
            return ExitCode.OK;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }
}
