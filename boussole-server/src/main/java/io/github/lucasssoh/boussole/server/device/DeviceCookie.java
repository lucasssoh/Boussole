package io.github.lucasssoh.boussole.server.device;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

/**
 * The key of a paired device, kept by its browser: sent only over HTTPS, only to Boussole, out of
 * reach of the page's scripts.
 */
public final class DeviceCookie {

    static final String NAME = "boussole_device";
    private static final Duration KEPT = Duration.ofDays(3650);

    private DeviceCookie() {
    }

    public static Optional<String> read(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies()).stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> cookie.getName().equals(NAME))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    static String header(String key) {
        return ResponseCookie.from(NAME, key).httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(KEPT).build().toString();
    }
}
