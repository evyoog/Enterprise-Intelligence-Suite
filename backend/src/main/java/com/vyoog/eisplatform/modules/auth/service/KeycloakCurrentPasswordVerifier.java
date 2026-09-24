package com.vyoog.eisplatform.modules.auth.service;

import org.springframework.stereotype.Service;

/** The real implementation — reuses the exact same Keycloak password grant
 * every normal login already goes through, discarding the resulting
 * tokens (see CurrentPasswordVerifier's own javadoc). */
@Service
public class KeycloakCurrentPasswordVerifier implements CurrentPasswordVerifier {

    private final KeycloakPasswordGrantService keycloakPasswordGrantService;

    public KeycloakCurrentPasswordVerifier(KeycloakPasswordGrantService keycloakPasswordGrantService) {
        this.keycloakPasswordGrantService = keycloakPasswordGrantService;
    }

    @Override
    public void verify(String email, String currentPassword) {
        keycloakPasswordGrantService.passwordGrant(email, currentPassword);
    }
}
