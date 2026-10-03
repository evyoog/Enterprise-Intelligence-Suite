package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.integration.controller.ApiKeyController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REQ-INT-001.2 (C61): fixed one-minute windows per API key, per signed-in
 * user, or per client IP for unauthenticated requests (BR-6). Counted in this
 * instance's memory. Every response carries {@code X-RateLimit-Limit} and
 * {@code X-RateLimit-Remaining}; over the limit: 429 with {@code Retry-After}.
 * Runs after authentication, so it knows which bucket a request belongs to.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;

    private final int perKey;
    private final int perUser;
    private final int perIp;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(int perKey, int perUser, int perIp, Clock clock) {
        this.perKey = perKey;
        this.perUser = perUser;
        this.perIp = perIp;
        this.clock = clock;
    }

    private static final class Window {
        final long start;
        int count;

        Window(long start) {
            this.start = start;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String bucket;
        int limit;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            if (jwt.hasClaim(ApiKeyController.API_KEY_CLAIM)) {
                bucket = "key:" + jwt.getClaimAsString(ApiKeyController.API_KEY_CLAIM);
                limit = perKey;
            } else {
                bucket = "user:" + jwt.getSubject();
                limit = perUser;
            }
        } else {
            bucket = "ip:" + request.getRemoteAddr();
            limit = perIp;
        }

        long now = clock.millis();
        long windowStart = now - (now % WINDOW_MILLIS);
        int used;
        synchronized (windows) {
            Window window = windows.get(bucket);
            if (window == null || window.start != windowStart) {
                window = new Window(windowStart);
                windows.put(bucket, window);
                if (windows.size() > 50_000) {
                    windows.values().removeIf(w -> w.start != windowStart);
                }
            }
            window.count++;
            used = window.count;
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - used)));
        if (used > limit) {
            long retryAfter = Math.max(1, (windowStart + WINDOW_MILLIS - now + 999) / 1000);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            ErrorResponses.write(response, 429, "Too Many Requests", "RATE_LIMITED",
                "Too many requests. Try again in " + retryAfter + " seconds.");
            return;
        }
        chain.doFilter(request, response);
    }
}
