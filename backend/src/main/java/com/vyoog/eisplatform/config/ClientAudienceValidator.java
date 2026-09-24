package com.vyoog.eisplatform.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Accepts a token only if this app's own Keycloak client id appears in either
 * {@code aud} or {@code azp} — matching {@link KeycloakJwtAuthenticationConverter}'s
 * own "check both, since which one carries the client id depends on whether an
 * Audience mapper is configured" reasoning exactly, so acceptance and role-source
 * logic never disagree. Without this, a validly-signed, unexpired token minted for
 * ANY client in the shared `eVyoog` realm (vyg-pms, vyg-requirement, vyg-ticket, the
 * impersonation-broker client, ...) would be accepted by this backend's protected
 * endpoints — see {@link JwtDecoderConfig}.
 */
public final class ClientAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR =
        new OAuth2Error("invalid_token", "Required audience is missing", null);

    private final String expectedClientId;

    public ClientAudienceValidator(String expectedClientId) {
        this.expectedClientId = expectedClientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        String azp = jwt.getClaimAsString("azp");
        boolean matches = (audiences != null && audiences.contains(expectedClientId))
            || expectedClientId.equals(azp);
        return matches ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(ERROR);
    }
}
