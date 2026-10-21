package io.github.lucasssoh.boussole.core.reference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Looks at a web page cited by a course, so that the interface shows it beside the text when the
 * site allows it, and otherwise offers to open it in a tab. Only web addresses are looked at.
 */
public final class PreviewWebPage {

    private final WebPages pages;

    public PreviewWebPage(WebPages pages) {
        this.pages = pages;
    }

    public PagePreview preview(String address) {
        try {
            URI uri = new URI(address);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("https") || scheme.equals("http")) || uri.getHost() == null) {
                return PagePreview.UNREACHABLE;
            }
            return pages.preview(uri);
        } catch (URISyntaxException e) {
            return PagePreview.UNREACHABLE;
        }
    }
}
