package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1 hardening: {@link ClientAudienceValidator} is what makes the JWT decoder
 * reject a validly-signed, unexpired token minted for a DIFFERENT Keycloak client in
 * the shared `eVyoog` realm (e.g. vyg-pms's own client) — before this validator
 * existed, only signature/issuer/timestamp were checked, so any such token would have
 * been accepted here.
 */
class ClientAudienceValidatorTest {

    private final ClientAudienceValidator validator = new ClientAudienceValidator("eis-platform-ui");

    @Test
    void acceptsTokenWhoseAudienceContainsThisAppsClientId() {
        Jwt jwt = jwtWith(List.of("eis-platform-ui"), null);

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void acceptsTokenWhoseAzpIsThisAppsClientIdEvenWithNoAudienceClaim() {
        // Mirrors KeycloakJwtAuthenticationConverter's own reasoning: whether the
        // client id lands in "aud" depends on whether an Audience mapper is
        // configured on that Keycloak client — azp alone must be enough.
        Jwt jwt = jwtWith(null, "eis-platform-ui");

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenMintedForADifferentClient() {
        // The exact case this validator exists to close: a token issued to a
        // sibling app's own client (e.g. vyg-pms's "eis-pms-ui"), validly signed
        // by the same shared Keycloak realm, must not be accepted here.
        Jwt jwt = jwtWith(List.of("eis-pms-ui"), "eis-pms-ui");

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenWithNeitherAudienceNorAzpClaim() {
        Jwt jwt = jwtWith(null, null);

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    private static Jwt jwtWith(List<String> audience, String azp) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject("some-keycloak-sub")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60));
        if (audience != null) {
            builder.audience(audience);
        }
        if (azp != null) {
            builder.claim("azp", azp);
        }
        return builder.build();
    }
}
