package com.vyoog.eisplatform.modules.auth.service;

import java.util.List;
import java.util.Optional;

/** Kept as an interface (matches this codebase's own {@code EmailService}/
 * {@code SmtpEmailService} precedent) purely so tests can substitute a fake
 * in-memory implementation instead of the real one making live HTTP calls
 * to Keycloak — see {@link HttpKeycloakAdminClient} for the real
 * implementation and its own javadoc on how the underlying capability was
 * verified before this was written. */
public interface KeycloakAdminClient {

    Optional<String> findUserIdByEmail(String email);

    boolean resetPassword(String keycloakUserId, String newPassword);

    void logoutAllSessions(String keycloakUserId);

    /**
     * Creates a real Keycloak user directly with a permanent (non-temporary)
     * credential and {@code emailVerified: true} — this app has already
     * verified the email itself via its own token flow, so Keycloak doesn't
     * need to separately gate on it. {@code firstName}/{@code lastName} must
     * both be non-blank: this realm's own User Profile configuration
     * requires them, and Keycloak silently refuses even a correct-password
     * login ("Account is not fully set up") for a user missing either —
     * confirmed live while diagnosing a real account in this state. Returns
     * the new user's id ({@code sub}), or empty on any failure.
     */
    Optional<String> createUser(String email, String firstName, String lastName, String password, boolean enabled);

    /** Flips only the enabled flag — used to activate an account created
     * disabled (see {@link #createUser}) once whatever gate held it back
     * (here, organization email verification) is satisfied. Returns false
     * rather than throwing on failure, same convention as
     * {@link #resetPassword}. */
    boolean setEnabled(String keycloakUserId, boolean enabled);

    /** Phase 3 (2026.3.3): one real Keycloak SSO session — {@code id} is
     * what {@link #revokeSession} takes, distinct from the user's own id.
     * Verified live against the real realm before this was written (see
     * SessionController's own javadoc). */
    record KeycloakSessionInfo(String id, String ipAddress, long startedAtEpochMillis,
                                long lastAccessAtEpochMillis, List<String> clients) {
    }

    /** All of this user's current real Keycloak sessions across every
     * client/app, oldest info first as Keycloak itself returns them. Empty
     * list (never throws) if the user has none or Keycloak is unreachable —
     * matching this interface's existing "fail closed to an empty/false
     * result" convention. */
    List<KeycloakSessionInfo> listSessions(String keycloakUserId);

    /** Ends exactly one Keycloak session by its own id — NOT the user id.
     * Callers must verify the session actually belongs to the caller
     * (via {@link #listSessions}) before calling this; this method itself
     * has no way to check ownership. Returns false rather than throwing. */
    boolean revokeSession(String sessionId);
}
