package com.vyoog.eisplatform.modules.renewal.dto;

/** REQ-SUB-004.8: the caller's settings and what applies (own value, else platform default). */
public record RenewalReminderPreferenceDto(
    boolean enabled,
    Integer daysBefore,
    String sendTime,
    int effectiveDaysBefore,
    String effectiveSendTime,
    String effectiveTimeZone,
    int platformDaysBefore,
    String platformSendTime,
    int minDays,
    int maxDays
) {
}
