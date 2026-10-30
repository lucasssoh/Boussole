package io.github.lucasssoh.boussole.cli.devices;

import tools.jackson.databind.JsonNode;
import io.github.lucasssoh.boussole.cli.ExitCode;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/** {@code boussole devices pair}: a code and its QR code for one device, then waits until it is used. */
@Command(name = "pair", mixinStandardHelpOptions = true, description = "Show a pairing code and its QR code, and wait for a device to use it.")
final class PairCommand implements Callable<Integer> {

    @Option(names = "--read-only", description = "The device reads but cannot answer.")
    private boolean readOnly;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws InterruptedException {
        PrintWriter out = spec.commandLine().getOut();
        LocalServer server = LocalServer.ofConfig();
        try {
            Set<String> before = ids(server.get("/api/devices"));
            JsonNode offer = server.post("/api/devices/pairing", Map.of("access", Devices.access(readOnly)));
            JsonNode network = server.get("/api/devices").path("network");
            String url = offer.path("urls").path(0).asText();
            out.print(QrText.of(url));
            out.printf("Address      %s%n", url.replaceFirst("\\?code=.*", ""));
            out.printf("Code         %s%n", Devices.code(offer.path("code").asText()));
            out.printf("Access       %s%n", offer.path("access").asText());
            out.printf("Expires      %s%n", Devices.time(offer.path("expiresAt")));
            out.printf("Fingerprint  %s%n", network.path("fingerprint").asText());
            out.println("Waiting. Ctrl+C to stop.");
            out.flush();
            Instant expires = Instant.parse(offer.path("expiresAt").asText());
            while (Instant.now().isBefore(expires)) {
                Thread.sleep(1000);
                for (JsonNode device : Devices.list(server.get("/api/devices"))) {
                    if (!before.contains(device.path("id").asText())) {
                        out.printf("Paired       %s  %s  %s%n", device.path("name").asText(), device.path("address").asText(), device.path("access").asText());
                        return ExitCode.OK;
                    }
                }
            }
            out.println("Code expired.");
            return ExitCode.PROBLEMS;
        } catch (DeviceCommandException e) {
            spec.commandLine().getErr().println(e.getMessage());
            return ExitCode.UNUSABLE;
        }
    }

    private static Set<String> ids(JsonNode devices) {
        return Devices.list(devices).stream().map(device -> device.path("id").asText()).collect(Collectors.toSet());
    }
}
