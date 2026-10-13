package com.vyoog.eisplatform.modules.administration.dto;

import java.time.Instant;

/** The platform defaults (REQ-GOV-002, C92, TM-2 A). Every field except {@code reminderDays} may be null = no default. */
public record PlatformDefaultsDto(
    String language,
    String region,
    String currency,
    String timeZone,
    String dateFormat,
    Integer reminderDays,
    Instant updatedAt
) {
}
