package com.vyoog.eisplatform.modules.auth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory stand-in for {@link HttpKeycloakAdminClient} — lets
 * {@code PasswordResetServiceTest}/{@code RegistrationServiceTest} exercise
 * the full token lifecycle/anti-enumeration/ordering logic without any test
 * in this suite ever making a live network call to Keycloak (matching every
 * other test in this codebase, which is real-DB-backed but never
 * network-backed).
 */
public class FakeKeycloakAdminClient implements KeycloakAdminClient {

    private final Map<String, String> usersByEmail = new HashMap<>();
    private final Map<String, Boolean> enabledByUserId = new HashMap<>();
    public final List<String> resetPasswordCalls = new ArrayList<>();
    public final List<String> logoutCalls = new ArrayList<>();
    public final List<CreateUserCall> createUserCalls = new ArrayList<>();
    public boolean resetPasswordShouldFail = false;
    public boolean createUserShouldFail = false;
    private int nextUserId = 1;
    private int nextSessionId = 1;
    private final Map<String, List<KeycloakSessionInfo>> sessionsByUserId = new HashMap<>();
    public final List<String> revokeSessionCalls = new ArrayList<>();

    public record CreateUserCall(String email, String firstName, String lastName, String password, boolean enabled) {
    }

    /** Adds a fake session for a user, returning its id — mirrors what a
     * real login would create in Keycloak. */
    public String addSession(String keycloakUserId, String ipAddress) {
        String sessionId = "fake-session-" + (nextSessionId++);
        sessionsByUserId.computeIfAbsent(keycloakUserId, k -> new ArrayList<>())
            .add(new KeycloakSessionInfo(sessionId, ipAddress, 0L, 0L, List.of("eis-platform-ui")));
        return sessionId;
    }

    public void registerUser(String email, String keycloakUserId) {
        usersByEmail.put(email.toLowerCase(), keycloakUserId);
    }

    public boolean isEnabled(String keycloakUserId) {
        return Boolean.TRUE.equals(enabledByUserId.get(keycloakUserId));
    }

    /** This bean is a singleton in the shared, cached Spring test context —
     * without resetting it, calls recorded by one test method would leak
     * into the next (JUnit doesn't guarantee method execution order). */
    public void reset() {
        usersByEmail.clear();
        enabledByUserId.clear();
        resetPasswordCalls.clear();
        logoutCalls.clear();
        createUserCalls.clear();
        resetPasswordShouldFail = false;
        createUserShouldFail = false;
        nextUserId = 1;
        nextSessionId = 1;
        sessionsByUserId.clear();
        revokeSessionCalls.clear();
    }

    @Override
    public Optional<String> findUserIdByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }

    @Override
    public boolean resetPassword(String keycloakUserId, String newPassword) {
        resetPasswordCalls.add(keycloakUserId);
        return !resetPasswordShouldFail;
    }

    @Override
    public void logoutAllSessions(String keycloakUserId) {
        logoutCalls.add(keycloakUserId);
    }

    @Override
    public Optional<String> createUser(String email, String firstName, String lastName, String password, boolean enabled) {
        createUserCalls.add(new CreateUserCall(email, firstName, lastName, password, enabled));
        if (createUserShouldFail) {
            return Optional.empty();
        }
        String id = "fake-kc-user-" + (nextUserId++);
        usersByEmail.put(email.toLowerCase(), id);
        enabledByUserId.put(id, enabled);
        return Optional.of(id);
    }

    @Override
    public boolean setEnabled(String keycloakUserId, boolean enabled) {
        enabledByUserId.put(keycloakUserId, enabled);
        return true;
    }

    @Override
    public List<KeycloakSessionInfo> listSessions(String keycloakUserId) {
        return sessionsByUserId.getOrDefault(keycloakUserId, List.of());
    }

    @Override
    public boolean revokeSession(String sessionId) {
        revokeSessionCalls.add(sessionId);
        boolean removedAny = false;
        for (List<KeycloakSessionInfo> sessions : sessionsByUserId.values()) {
            removedAny |= sessions.removeIf(s -> s.id().equals(sessionId));
        }
        return removedAny;
    }
}
