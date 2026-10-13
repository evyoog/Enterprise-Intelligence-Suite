package com.vyoog.eisplatform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REQ-INT-001 (C92, API-5 B): "Deprecate API". When an endpoint (or a whole version, for example {@code /v1/}) is
 * marked deprecated in {@code app.api.deprecations}, its responses carry {@code Deprecation: true}, {@code Sunset:
 * <removal date>} and, when a successor is configured, {@code Link: <successor>; rel="successor-version"}. Nothing is
 * deprecated today (only /v1 exists); the list is filled in when /v2 is introduced. Runs ahead of
 * {@link ApiVersionFilter}, so it sees the path exactly as the client sent it.
 */
public class ApiDeprecationFilter extends OncePerRequestFilter {

    /** One deprecated path prefix as the client sees it, its removal date and the path that replaces it. */
    public record Rule(String pathPrefix, LocalDate sunset, String successor) {
    }

    private final List<Rule> rules;

    public ApiDeprecationFilter(List<Rule> rules) {
        this.rules = rules.stream().filter(r -> r.pathPrefix() != null && !r.pathPrefix().isBlank() && r.sunset() != null).toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (Rule rule : rules) {
            if (matches(path, rule.pathPrefix())) {
                response.setHeader("Deprecation", "true");
                response.setHeader("Sunset", DateTimeFormatter.RFC_1123_DATE_TIME.format(rule.sunset().atStartOfDay(ZoneOffset.UTC)));
                if (rule.successor() != null && !rule.successor().isBlank()) {
                    response.setHeader("Link", "<" + rule.successor() + ">; rel=\"successor-version\"");
                }
                break;
            }
        }
        chain.doFilter(request, response);
    }

    /** {@code /v1/} matches {@code /v1} and everything under it; {@code /orders} matches {@code /orders} and {@code /orders/...}. */
    static boolean matches(String path, String prefix) {
        String p = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return path.equals(p) || path.startsWith(p + "/");
    }
}
