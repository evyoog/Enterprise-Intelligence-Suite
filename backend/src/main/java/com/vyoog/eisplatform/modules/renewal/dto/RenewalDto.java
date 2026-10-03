package com.vyoog.eisplatform.modules.renewal.dto;

import java.time.Instant;

/** REQ-SUB-004: one of the caller's renewals. {@code nextReminderAt} is in
 * the caller's time zone (ISO with offset), or null when reminders are off
 * or none is still to come. */
public record RenewalDto(
    Long subscriptionId,
    String productName,
    String planName,
    boolean autoRenew,
    Instant renewalDate,
    boolean remindersEnabled,
    String nextReminderAt
) {
}
