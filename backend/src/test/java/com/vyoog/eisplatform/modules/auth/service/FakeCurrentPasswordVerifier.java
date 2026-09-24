package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory stand-in for {@link KeycloakCurrentPasswordVerifier} — lets
 * {@code PlatformMfaServiceTest} exercise every re-authentication branch
 * without a real network call to Keycloak (same reasoning as
 * FakeKeycloakAdminClient).
 */
public class FakeCurrentPasswordVerifier implements CurrentPasswordVerifier {

    public final List<String> verifiedEmails = new ArrayList<>();
    public boolean shouldFail = false;

    /** This bean is a singleton in the shared, cached Spring test context —
     * without resetting it, calls recorded by one test method would leak
     * into the next. */
    public void reset() {
        verifiedEmails.clear();
        shouldFail = false;
    }

    @Override
    public void verify(String email, String currentPassword) {
        if (shouldFail) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        verifiedEmails.add(email);
    }
}
