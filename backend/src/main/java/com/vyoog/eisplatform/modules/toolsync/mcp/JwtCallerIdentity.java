package com.vyoog.eisplatform.modules.toolsync.mcp;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Reads the {@code azp} from the request's validated JWT (the filter chain has already checked signature, issuer, expiry and audience). */
@Component
public class JwtCallerIdentity implements CallerIdentity {

    @Override
    public String clientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token && token.getToken() instanceof Jwt jwt) {
            return jwt.getClaimAsString("azp");
        }
        return null;
    }
}
