package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.dto.SessionDto;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient.KeycloakSessionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Phase 3 (2026.3.3): self-service Keycloak session management — always
 * scoped to the CALLER's own Keycloak identity (the JWT's own {@code sub}),
 * never a client-supplied user or session id. Ownership of a specific
 * session is enforced the only way it safely can be here: by re-fetching
 * the caller's own session list from Keycloak and checking the requested id
 * is actually in it, BEFORE ever calling revoke — a client cannot revoke a
 * session it doesn't already own just by guessing/supplying another user's
 * session id, since that id would never appear in the caller's own list.
 */
@RestController
@RequestMapping("/me/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final KeycloakAdminClient keycloakAdminClient;
    private final AuditService auditService;

    @GetMapping
    public List<SessionDto> listSessions(@AuthenticationPrincipal Jwt jwt) {
        String currentSessionId = jwt.getClaimAsString("sid");
        return keycloakAdminClient.listSessions(jwt.getSubject()).stream()
            .map(session -> toDto(session, currentSessionId))
            .toList();
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(@AuthenticationPrincipal Jwt jwt, @PathVariable String sessionId) {
        String sub = jwt.getSubject();
        boolean ownsSession = keycloakAdminClient.listSessions(sub).stream()
            .anyMatch(session -> session.id().equals(sessionId));
        if (!ownsSession) {
            // Deliberately the same generic message a genuinely-missing
            // session id would get — never confirm/deny whether a given
            // session id belongs to someone else.
            throw new ForbiddenException("You do not have permission to do this");
        }
        keycloakAdminClient.revokeSession(sessionId);
        auditService.recordSuccess("SESSION_REVOKED", sub, null, null, "KeycloakSession", sessionId, null, null);
    }

    private static SessionDto toDto(KeycloakSessionInfo session, String currentSessionId) {
        return new SessionDto(
            session.id(),
            session.ipAddress(),
            session.startedAtEpochMillis() > 0 ? Instant.ofEpochMilli(session.startedAtEpochMillis()) : null,
            session.lastAccessAtEpochMillis() > 0 ? Instant.ofEpochMilli(session.lastAccessAtEpochMillis()) : null,
            session.clients(),
            session.id().equals(currentSessionId)
        );
    }
}
