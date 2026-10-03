package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** REQ-SUB-004 BR-7: days 1–30, time HH:mm (24-hour), a valid IANA time zone. */
public record SaveRenewalReminderDefaultsRequest(
    @NotNull @Min(1) @Max(30) Integer daysBefore,
    @NotBlank @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "must be HH:mm") String sendTime,
    @NotBlank String timeZone
) {
}
