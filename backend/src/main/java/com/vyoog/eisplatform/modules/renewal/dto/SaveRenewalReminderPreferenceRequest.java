package com.vyoog.eisplatform.modules.renewal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** REQ-SUB-004.8 (BR-7): null days or time = use the platform default. */
public record SaveRenewalReminderPreferenceRequest(
    @NotNull Boolean enabled,
    @Min(1) @Max(30) Integer daysBefore,
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "must be HH:mm") String sendTime
) {
}
