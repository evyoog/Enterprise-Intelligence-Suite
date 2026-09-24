package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.modules.auth.model.SsoBridgeSession;
import com.vyoog.eisplatform.modules.auth.repository.SsoBridgeSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * The cross-app "already logged in" marker (see SsoBridgeSession's own javadoc).
 * Lives entirely on this backend — the id handed to the browser is a random
 * opaque string, never a Keycloak token.
 */
@Service
public class SsoBridgeSessionService {

    /** Generous window: this only gates how long the *other* app can silently
     * pick up the session, not how long this app's own login lasts (that's
     * governed by the real refresh-token cookie, refreshed independently). */
    private static final Duration BRIDGE_LIFETIME = Duration.ofHours(12);

    private final SsoBridgeSessionRepository repository;
    private final SecureRandom random = new SecureRandom();

    public SsoBridgeSessionService(SsoBridgeSessionRepository repository) {
        this.repository = repository;
    }

    /** Real login via this app's own client — refreshToken belongs to it. */
    @Transactional
    public String create(String keycloakSub, String refreshToken) {
        return createOrUpdate(generateId(), keycloakSub, refreshToken, false);
    }

    /**
     * Phase 5: same as {@link #create}, but for a session whose refresh
     * token came from an {@link ImpersonationExchangeService} exchange
     * rather than this app's own ROPC client (e.g. a freshly-established
     * SAML login — see SamlAuthenticationService) — impersonated=true from
     * the start, on a freshly-generated id, so this app's own later
     * refresh/session calls correctly re-exchange instead of attempting a
     * normal refresh grant with the wrong client credentials (see
     * AuthController#refreshWithRightCredentials).
     */
    @Transactional
    public String createImpersonated(String keycloakSub, String refreshToken) {
        return createOrUpdate(generateId(), keycloakSub, refreshToken, true);
    }

    /**
     * Caches this app's OWN copy of a bridge session under an id that was
     * actually minted by the *other* app, after this app redeemed it via
     * ImpersonationExchangeService — so a later visit here can refresh
     * directly instead of exchanging again every time. impersonated=true
     * always here, since anything reaching this overload came from an
     * exchange, never a real login.
     */
    @Transactional
    public String createOrUpdate(String id, String keycloakSub, String refreshToken) {
        return createOrUpdate(id, keycloakSub, refreshToken, true);
    }

    private String createOrUpdate(String id, String keycloakSub, String refreshToken, boolean impersonated) {
        SsoBridgeSession session = repository.findById(id).orElseGet(SsoBridgeSession::new);
        session.setId(id);
        session.setKeycloakSub(keycloakSub);
        session.setRefreshToken(refreshToken);
        session.setImpersonated(impersonated);
        session.setCreatedAt(session.getCreatedAt() == null ? Instant.now() : session.getCreatedAt());
        session.setExpiresAt(Instant.now().plus(BRIDGE_LIFETIME));
        repository.save(session);
        return session.getId();
    }

    public Optional<SsoBridgeSession> find(String sessionId) {
        return repository.findById(sessionId)
            .filter(s -> s.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public void updateRefreshToken(String sessionId, String newRefreshToken) {
        repository.findById(sessionId).ifPresent(s -> s.setRefreshToken(newRefreshToken));
    }

    @Transactional
    public void delete(String sessionId) {
        repository.deleteById(sessionId);
    }

    private String generateId() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
