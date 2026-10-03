package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** REQ-INT-001.2 (C61, BR-6): one bucket per key, user or IP; 429 with
 * Retry-After once the minute's limit is used. TC-INT-004. */
class RateLimitFilterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-03T09:00:15Z"), ZoneOffset.UTC);
    private final RateLimitFilter filter = new RateLimitFilter(2, 3, 1, clock);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse call(String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private void signIn(String sub, Long apiKeyId) {
        Jwt.Builder jwt = Jwt.withTokenValue("t").header("alg", "none").subject(sub);
        if (apiKeyId != null) {
            jwt.claim("api_key_id", apiKeyId);
        }
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt.build(), List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void anonymousRequestsAreLimitedPerIp() throws Exception {
        MockHttpServletResponse first = call("10.0.0.1");
        assertThat(first.getStatus()).isEqualTo(200);
        assertThat(first.getHeader("X-RateLimit-Limit")).isEqualTo("1");
        assertThat(first.getHeader("X-RateLimit-Remaining")).isEqualTo("0");

        MockHttpServletResponse second = call("10.0.0.1");
        assertThat(second.getStatus()).isEqualTo(429);
        assertThat(second.getHeader("Retry-After")).isEqualTo("45");
        assertThat(second.getContentAsString()).contains("\"code\":\"RATE_LIMITED\"");

        assertThat(call("10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void signedInUsersAndKeysHaveTheirOwnBuckets() throws Exception {
        signIn("user-a", null);
        for (int i = 0; i < 3; i++) {
            assertThat(call("10.0.0.3").getStatus()).isEqualTo(200);
        }
        assertThat(call("10.0.0.3").getStatus()).isEqualTo(429);

        signIn("user-a", 9L);
        assertThat(call("10.0.0.3").getHeader("X-RateLimit-Limit")).isEqualTo("2");
        assertThat(call("10.0.0.3").getStatus()).isEqualTo(200);
        assertThat(call("10.0.0.3").getStatus()).isEqualTo(429);
    }
}
