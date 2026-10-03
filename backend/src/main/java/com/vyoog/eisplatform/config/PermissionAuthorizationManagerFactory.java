package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Phase 3: what {@code SecurityConfig}'s literal {@code hasRole("ADMIN")}
 * calls became. {@code hasRole} only ever asks "does the JWT carry this exact
 * authority string" — this instead asks {@link AuthorizationService} "does
 * whatever role(s) the caller's authorities map to actually grant this named
 * permission," which is real, DB-editable data (see {@code Role}/{@code Permission}).
 * Today the two questions have the same answer for every existing endpoint
 * (see the RbacSeeder's PLATFORM_ADMIN==ADMIN seed data) — the difference is
 * that answer now lives in a table, not a string literal repeated across
 * SecurityConfig.
 *
 * <p>Phase 6 (PAM): also grants access when the caller holds a currently-
 * active, approved privileged-access request for this exact permission —
 * see {@link PrivilegedAccessService#hasActivePlatformGrant}. This is the
 * one place a PLATFORM-scope temporary grant actually takes effect, since
 * every platform-admin-gated endpoint in {@code SecurityConfig} is checked
 * through this factory.
 */
@Component
@RequiredArgsConstructor
public class PermissionAuthorizationManagerFactory {

    private final AuthorizationService authorizationService;
    private final PrivilegedAccessService privilegedAccessService;

    public AuthorizationManager<RequestAuthorizationContext> platformPermission(String permissionName) {
        return (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
            Authentication auth = authentication.get();
            if (auth == null || !auth.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }
            List<GrantedAuthority> authorities = List.copyOf(auth.getAuthorities());
            List<String> authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();
            // C61 (REQ-INT-001 BR-3): platform administration is never granted
            // to an API key, not even through an active privileged-access grant.
            if (auth.getPrincipal() instanceof Jwt keyJwt && keyJwt.hasClaim("api_key_id")) {
                return new AuthorizationDecision(false);
            }
            String keycloakSub = auth.getPrincipal() instanceof Jwt jwt ? jwt.getSubject() : null;
            boolean granted = authorizationService.hasPlatformPermission(authorityNames, permissionName)
                || privilegedAccessService.hasActivePlatformGrant(keycloakSub, permissionName);
            return new AuthorityAuthorizationDecision(granted, authorities);
        };
    }
}
