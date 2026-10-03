package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** REQ-SUB-004.4/.5 (C64): the platform's renewal reminder defaults. */
public record RenewalReminderDefaultsDto(int daysBefore, String sendTime, String timeZone, Instant updatedAt) {
}
