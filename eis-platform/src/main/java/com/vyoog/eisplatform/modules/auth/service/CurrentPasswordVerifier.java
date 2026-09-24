package com.vyoog.eisplatform.modules.auth.service;

/** Proves "this caller still knows the current password right now" — used
 * before every state-changing Platform MFA action (see PlatformMfaService's
 * own javadoc). Deliberately its own interface (same pattern as
 * KeycloakAdminClient) rather than a direct dependency on
 * KeycloakPasswordGrantService, so tests can fake it without making a real
 * network call to Keycloak. */
public interface CurrentPasswordVerifier {

    /** Throws (a runtime exception) if the password is wrong — never
     * returns a token, only proves the password is still correct. */
    void verify(String email, String currentPassword);
}
