package com.vyoog.eisplatform.modules.authorization.dto;

import com.vyoog.eisplatform.modules.authorization.model.RoleScope;

import java.time.Instant;
import java.util.List;

/**
 * {@code effectiveStatus} is a display-only string — "EXPIRED" when the
 * stored status is still APPROVED but {@code expiresAt} has passed (see
 * {@code PrivilegedAccessStatus}'s own javadoc on why that's computed, not
 * stored) — everywhere else it's just the stored status name.
 */
public record PrivilegedAccessRequestDto(
    Long id,
    RoleScope scope,
    Long organizationId,
    String permissionName,
    String justification,
    String status,
    String effectiveStatus,
    Instant requestedAt,
    int requestedDurationMinutes,
    Instant decidedAt,
    String decidedByKeycloakSub,
    String decisionNote,
    Instant expiresAt,
    List<PrivilegedAccessAuditEntryDto> auditTrail
) {
}
