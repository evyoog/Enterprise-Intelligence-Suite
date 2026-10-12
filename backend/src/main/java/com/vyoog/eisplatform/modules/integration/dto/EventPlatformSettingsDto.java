package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;
import java.util.List;

/** The event platform settings (REQ-INT-002, C91). */
public record EventPlatformSettingsDto(
    int retentionDays,
    List<String> alertRecipients,
    boolean alertInApp,
    boolean alertEmail,
    Instant updatedAt
) {
}
