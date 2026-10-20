package io.github.lucasssoh.boussole.server.api;

import io.github.lucasssoh.boussole.server.ServerSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets {@code boussole open} find a running server, and {@code boussole stop} stop it. */
@RestController
@RequestMapping("/api/server")
class ServerController {

    /** The name {@code boussole open} looks for, so another program on the port is not mistaken for Boussole. */
    static final String NAME = "boussole";

    private final ConfigurableApplicationContext context;
    private final ServerSettings settings;

    ServerController(ConfigurableApplicationContext context, ServerSettings settings) {
        this.context = context;
        this.settings = settings;
    }

    @GetMapping
    ServerView describe() {
        String version = BoussoleVersion.current();
        return new ServerView(NAME, version, settings.language().orElse(null), settings.freeNavigation());
    }

    @PostMapping("/stop")
    void stop() {
        Thread.ofPlatform().start(() -> SpringApplication.exit(context, () -> 0));
    }

    /** {@code freeNavigation}: a lesson lets the learner go on without answering its questions. */
    record ServerView(String name, String version, String language, boolean freeNavigation) {
    }
}
