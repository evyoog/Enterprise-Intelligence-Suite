package com.vyoog.eisplatform.modules.administration.dto;

/** A blank text clears that default. {@code reminderDays} (1 to 30) is optional; missing leaves it unchanged. */
public record SavePlatformDefaultsRequest(
    String language,
    String region,
    String currency,
    String timeZone,
    String dateFormat,
    Integer reminderDays
) {
}
