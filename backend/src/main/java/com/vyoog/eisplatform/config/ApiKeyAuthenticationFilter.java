package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.integration.controller.ApiKeyController;
import com.vyoog.eisplatform.modules.integration.service.ApiKeyPrincipal;
import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * REQ-INT-001.1 (C61): authenticates {@code X-API-Key} requests (ignored when
 * an {@code Authorization} header is present). A valid key acts as its owner:
 * the principal is a {@link Jwt} carrying the owner's subject (so every
 * {@code @AuthenticationPrincipal Jwt} controller works unchanged) and the
 * key's copied authorities, never ROLE_ADMIN. Platform administration
 * ({@code /admin/**}) is refused with 403 (BR-3). An unknown, revoked or
 * expired key gets 401 (BR-2).
 *
 * <p>Not a Spring bean on purpose: it is added only to the security filter
 * chain, so it is never also registered as a plain servlet filter.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String presented = request.getHeader(HEADER);
        if (presented == null || presented.isBlank() || request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            chain.doFilter(request, response);
            return;
        }
        Optional<ApiKeyPrincipal> principal = apiKeyService.authenticate(presented);
        if (principal.isEmpty()) {
            ErrorResponses.write(response, 401, "Unauthorized", "API_KEY_INVALID", "The API key is invalid, revoked or expired.");
            return;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            ErrorResponses.write(response, 403, "Forbidden", null, "Platform administration is not available with an API key.");
            return;
        }
        ApiKeyPrincipal p = principal.get();
        Instant now = Instant.now();
        Jwt.Builder jwt = Jwt.withTokenValue("api-key:" + p.keyPrefix())
            .header("alg", "none")
            .subject(p.ownerKeycloakSub())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(60))
            .claim(ApiKeyController.API_KEY_CLAIM, p.keyId());
        if (p.ownerEmail() != null) {
            jwt.claim("email", p.ownerEmail());
        }
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt.build(),
            p.authorities().stream().map(SimpleGrantedAuthority::new).toList(), p.ownerKeycloakSub());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }
}
