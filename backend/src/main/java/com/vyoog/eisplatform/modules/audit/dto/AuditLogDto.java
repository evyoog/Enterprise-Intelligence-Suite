package com.vyoog.eisplatform.modules.audit.dto;

import java.time.Instant;

public record AuditLogDto(
    Long id,
    Instant timestamp,
    String action,
    Long actorCustomerId,
    String actorEmail,
    String targetType,
    String targetId,
    Long organizationId,
    String outcome,
    String detail
) {
}
