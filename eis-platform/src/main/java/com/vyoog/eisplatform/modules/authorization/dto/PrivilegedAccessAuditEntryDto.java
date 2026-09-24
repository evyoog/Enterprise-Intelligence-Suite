package com.vyoog.eisplatform.modules.authorization.dto;

import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessEventType;

import java.time.Instant;

public record PrivilegedAccessAuditEntryDto(
    PrivilegedAccessEventType eventType,
    String actorKeycloakSub,
    Instant occurredAt,
    String note
) {
}
