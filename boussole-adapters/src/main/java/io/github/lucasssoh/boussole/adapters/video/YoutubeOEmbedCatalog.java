package io.github.lucasssoh.boussole.adapters.video;

import io.github.lucasssoh.boussole.format.validate.VideoCatalog;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Asks YouTube's public oEmbed endpoint whether a video exists: no key, no account. A removed or
 * private video answers 401, 403 or 404.
 */
public final class YoutubeOEmbedCatalog implements VideoCatalog {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    @Override
    public boolean exists(String youtubeId) {
        URI uri = URI.create("https://www.youtube.com/oembed?format=json&url=https://www.youtube.com/watch?v=" + youtubeId);
        try {
            HttpResponse<Void> response = client.send(
                    HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build(), HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException e) {
            throw new IllegalStateException("cannot reach YouTube to check " + youtubeId + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while checking " + youtubeId, e);
        }
    }
}
