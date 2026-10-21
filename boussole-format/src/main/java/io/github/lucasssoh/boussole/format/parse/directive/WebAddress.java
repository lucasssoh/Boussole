package io.github.lucasssoh.boussole.format.parse.directive;

import io.github.lucasssoh.boussole.format.parse.InvalidAttributeException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** The {@code url} of a {@code link}: a whole web address, {@code https://} or {@code http://}. */
public final class WebAddress {

    private WebAddress() {
    }

    public static String read(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if ((scheme.equals("https") || scheme.equals("http")) && uri.getHost() != null) {
                return value;
            }
        } catch (URISyntaxException e) {
            // Reported below, like any other address that is not a web page.
        }
        throw new InvalidAttributeException("url", "must be a whole web address, such as `https://docs.python.org/3/`, not `" + value + "`");
    }
}
