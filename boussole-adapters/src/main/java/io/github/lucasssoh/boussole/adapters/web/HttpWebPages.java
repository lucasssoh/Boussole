package io.github.lucasssoh.boussole.adapters.web;

import io.github.lucasssoh.boussole.core.reference.PagePreview;
import io.github.lucasssoh.boussole.core.reference.WebPages;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks at a web page over HTTP: whether it answers, whether its headers let it be shown in a
 * frame ({@code X-Frame-Options}, {@code frame-ancestors} of {@code Content-Security-Policy}),
 * and the title of its first kilobytes. Addresses of this machine or its local network are not
 * looked at, so that a course cannot make Boussole probe them.
 */
public final class HttpWebPages implements WebPages {

    private static final Duration TIMEOUT = Duration.ofSeconds(6);
    private static final int HEAD_BYTES = 64 * 1024;
    private static final Pattern TITLE = Pattern.compile("<title[^>]*>([^<]{1,300})</title>", Pattern.CASE_INSENSITIVE);
    private static final Pattern FRAME_ANCESTORS = Pattern.compile("frame-ancestors([^;]*)", Pattern.CASE_INSENSITIVE);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final boolean localNetworkAllowed;

    public HttpWebPages() {
        this(false);
    }

    /** {@code localNetworkAllowed}: for tests only, which serve pages on this machine. */
    HttpWebPages(boolean localNetworkAllowed) {
        this.localNetworkAllowed = localNetworkAllowed;
    }

    @Override
    public PagePreview preview(URI page) {
        if (!localNetworkAllowed && isLocal(page.getHost())) {
            return PagePreview.UNREACHABLE;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(page).timeout(TIMEOUT)
                    .header("Accept", "text/html,*/*;q=0.8")
                    .header("User-Agent", "Boussole (a local study app checking a page its course cites)")
                    .GET().build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() >= 400) {
                    return PagePreview.UNREACHABLE;
                }
                String head = new String(body.readNBytes(HEAD_BYTES), StandardCharsets.UTF_8);
                return new PagePreview(true, embeddable(response.headers()), title(head));
            }
        } catch (IOException | IllegalArgumentException e) {
            return PagePreview.UNREACHABLE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PagePreview.UNREACHABLE;
        }
    }

    static boolean embeddable(HttpHeaders headers) {
        boolean deniedByOptions = headers.allValues("X-Frame-Options").stream()
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.equals("DENY") || value.equals("SAMEORIGIN"));
        boolean deniedByPolicy = headers.allValues("Content-Security-Policy").stream().anyMatch(policy -> {
            Matcher matcher = FRAME_ANCESTORS.matcher(policy);
            return matcher.find() && !Arrays.asList(matcher.group(1).trim().split("\\s+")).contains("*");
        });
        return !deniedByOptions && !deniedByPolicy;
    }

    private static Optional<String> title(String head) {
        Matcher matcher = TITLE.matcher(head);
        return matcher.find() ? Optional.of(unescape(matcher.group(1).strip())).filter(text -> !text.isEmpty()) : Optional.empty();
    }

    private static String unescape(String text) {
        return text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
    }

    private static boolean isLocal(String host) {
        try {
            return Arrays.stream(InetAddress.getAllByName(host)).anyMatch(address -> address.isLoopbackAddress()
                    || address.isSiteLocalAddress() || address.isLinkLocalAddress() || address.isAnyLocalAddress());
        } catch (UnknownHostException e) {
            return true;
        }
    }
}
