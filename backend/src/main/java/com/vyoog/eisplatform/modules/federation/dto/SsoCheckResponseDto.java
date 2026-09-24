package com.vyoog.eisplatform.modules.federation.dto;

/**
 * Phase 5: response to the pre-login {@code GET /saml/sso-check} the SPA
 * calls (via {@code fetch}, before any navigation) so it can show "Sign in
 * with {organizationName}" inline rather than guessing — see
 * SamlAuthenticationService#checkSso. Deliberately reveals only whether SSO
 * is available and the organization's own public name, never anything about
 * the underlying IdP configuration.
 */
public record SsoCheckResponseDto(boolean available, Long organizationId, String organizationName) {
}
