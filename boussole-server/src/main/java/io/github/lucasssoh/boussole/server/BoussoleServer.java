package io.github.lucasssoh.boussole.server;

import java.util.Map;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/** The local HTTP server: the API under {@code /api}, and the interface everywhere else. */
@SpringBootApplication
public class BoussoleServer {

    /** Starts the server, listening on this machine only, and returns once it accepts requests. */
    public static RunningServer start(ServerSettings settings) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(BoussoleServer.class)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .properties(Map.of(
                        "server.port", settings.port(),
                        "server.address", "127.0.0.1",
                        "spring.main.register-shutdown-hook", true,
                        "logging.level.root", "warn"))
                .initializers(starting -> starting.getBeanFactory().registerSingleton("serverSettings", settings))
                .run();
        return new RunningServer(context, ((WebServerApplicationContext) context).getWebServer().getPort());
    }
}
