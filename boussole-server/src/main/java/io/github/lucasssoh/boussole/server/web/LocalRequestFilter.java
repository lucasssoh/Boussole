package io.github.lucasssoh.boussole.server.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Keeps the server for this machine's browser only. A web page elsewhere could otherwise reach it:
 * through another host name pointed at 127.0.0.1 (refused by the {@code Host} check), or by
 * posting a form to it (refused, since a form cannot send the {@code X-Boussole} header).
 */
@Component
class LocalRequestFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Boussole";
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");
    private static final Set<String> READS = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!isLocalHost(request.getHeader("Host"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Boussole only answers on localhost");
            return;
        }
        if (!READS.contains(request.getMethod()) && request.getHeader(HEADER) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "missing " + HEADER + " header");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isLocalHost(String host) {
        return Optional.ofNullable(host)
                .map(value -> value.replaceFirst(":\\d+$", ""))
                .filter(LOCAL_HOSTS::contains)
                .isPresent();
    }
}
