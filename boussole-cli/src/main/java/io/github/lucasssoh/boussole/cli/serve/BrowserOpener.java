package io.github.lucasssoh.boussole.cli.serve;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Opens a page in the learner's usual browser. */
final class BrowserOpener {

    private BrowserOpener() {
    }

    static boolean open(String url) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> command = os.contains("mac") ? List.of("open", url)
                : os.contains("win") ? List.of("rundll32", "url.dll,FileProtocolHandler", url)
                : List.of("xdg-open", url);
        try {
            new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
