package com.vyoog.eisplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

/**
 * Phase 1 hardening: Spring Boot's auto-configured resource-server {@code JwtDecoder}
 * validates signature, issuer, and timestamps (via {@code issuer-uri}/{@code jwk-set-uri})
 * but has no notion of audience at all — any token from ANY client in the shared
 * `eVyoog` realm (vyg-pms, vyg-requirement, vyg-ticket, the impersonation-broker
 * client, ...) would be accepted here as long as it's validly signed and unexpired.
 * This adds the missing check: a token presented to THIS backend's protected
 * endpoints must actually have been minted for THIS app's own client
 * ({@code vyoog.keycloak.ropc-client-id}) — checking both {@code aud} and
 * {@code azp}, the same two claims {@link KeycloakJwtAuthenticationConverter}
 * already checks for role extraction, so acceptance and role-source logic agree.
 *
 * Deliberately built the same way Spring Boot's own auto-configuration does when
 * both {@code issuer-uri} and {@code jwk-set-uri} are set (a plain
 * {@code NimbusJwtDecoder.withJwkSetUri(...)}, never {@code JwtDecoders.fromIssuerLocation(...)})
 * — that avoids an eager network call to the issuer's discovery endpoint at
 * context startup, which is exactly why the test profile's placeholder issuer
 * URL never gets called (see application-test.yml's own comment on this).
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${vyoog.keycloak.ropc-client-id}") String ownClientId) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> defaults = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audience = new ClientAudienceValidator(ownClientId);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaults, audience));

        return decoder;
    }
}
