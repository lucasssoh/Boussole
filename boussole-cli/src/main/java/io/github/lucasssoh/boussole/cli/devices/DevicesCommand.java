package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/** {@code boussole devices}: the network access, the pending requests and the paired devices. */
@Command(
        name = "devices",
        mixinStandardHelpOptions = true,
        description = "List the devices paired with this Boussole and the requests waiting.",
        subcommands = {PairCommand.class, AcceptCommand.class, DenyCommand.class, RemoveDeviceCommand.class, RenameDeviceCommand.class,
                AccessCommand.class})
public final class DevicesCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        try {
            JsonNode all = LocalServer.ofConfig().get("/api/devices");
            JsonNode network = all.path("network");
            if (network.path("enabled").asBoolean()) {
                out.printf("Network      on, port %d%n", network.path("port").asInt());
                network.path("hosts").forEach(host -> out.printf("Address      https://%s:%d/%n", host.asText(), network.path("port").asInt()));
                out.printf("Fingerprint  %s%n", network.path("fingerprint").asText());
            } else {
                out.println("Network      off (network: true in config.yaml)");
            }
            out.println();
            out.println("Requests");
            if (all.path("requests").isEmpty()) {
                out.println("  none");
            }
            all.path("requests").forEach(request -> out.printf("  %-8s  %-20s  %-15s  expires %s%n", Devices.code(request.path("code").asText()),
                    request.path("name").asText(), request.path("address").asText(), Devices.time(request.path("expiresAt"))));
            out.println();
            out.println("Devices");
            if (Devices.list(all).isEmpty()) {
                out.println("  none");
            }
            for (JsonNode device : Devices.list(all)) {
                out.printf("  %-20s  %-15s  %-9s  seen %s  %s%n", device.path("name").asText(), device.path("address").asText(),
                        device.path("access").asText(), Devices.time(device.path("lastSeen")), device.path("id").asText());
            }
            return ExitCode.OK;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }
}
