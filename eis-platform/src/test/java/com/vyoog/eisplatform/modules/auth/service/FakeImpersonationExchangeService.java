package com.vyoog.eisplatform.modules.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory stand-in for {@link ImpersonationExchangeService} — same
 * reasoning/convention as {@link FakeKeycloakAdminClient}: no test in this
 * suite makes a live network call to Keycloak. A plain subclass (this class
 * has no interface to implement — see the real class's own javadoc) with a
 * throwaway super-constructor, since none of its real HTTP fields are ever
 * touched once {@link #exchangeForUser} and {@link #logout} are overridden.
 */
public class FakeImpersonationExchangeService extends ImpersonationExchangeService {

    public final List<String> exchangeCalls = new ArrayList<>();
    public boolean shouldFail = false;

    public FakeImpersonationExchangeService() {
        super(new ObjectMapper(), "unused", "unused", "unused", "unused");
    }

    @Override
    public Optional<KeycloakPasswordGrantService.TokenResult> exchangeForUser(String keycloakSub, String targetClientId) {
        exchangeCalls.add(keycloakSub + ":" + targetClientId);
        if (shouldFail) {
            return Optional.empty();
        }
        return Optional.of(new KeycloakPasswordGrantService.TokenResult(
            "fake-access-token-for-" + keycloakSub, "fake-refresh-token-for-" + keycloakSub, 3600L));
    }

    @Override
    public void logout(String refreshToken) {
        // No-op — nothing real to end.
    }

    public void reset() {
        exchangeCalls.clear();
        shouldFail = false;
    }
}
