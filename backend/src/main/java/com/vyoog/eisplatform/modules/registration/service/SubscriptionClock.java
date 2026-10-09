package com.vyoog.eisplatform.modules.registration.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * BR-SUB-010 (decision Q11 / assumption A3): a subscription's end is the END OF ITS LAST DAY in India, {@code 23:59:00.000}
 * in {@code Asia/Kolkata} (+05:30, no daylight saving). The end is stored as that instant (the database holds UTC, so
 * {@code 18:29:00.000} on the same calendar date) and written in the contract of the platform ↔ tool synchronization as
 * {@code 2026-10-31T23:59:00.000+05:30}. A subscription is effective while {@code startsAt <= now <= endsAt}; one
 * millisecond later access is denied at once (no grace). Seconds are {@code :00}, not {@code :59}, exactly as decided.
 *
 * <p>Every place that sets or compares a subscription end goes through this class.
 */
public final class SubscriptionClock {

    /** The one zone subscription ends are defined in. */
    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    public static final ZoneOffset OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    private static final DateTimeFormatter CONTRACT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private SubscriptionClock() {
    }

    /** 23:59:00.000 on {@code date} in India, as an instant. */
    public static Instant endOfDay(LocalDate date) {
        return date.atTime(23, 59, 0, 0).atZone(ZONE).toInstant();
    }

    /** The end of the Indian calendar day that contains {@code moment}: use it for "start plus one billing period". */
    public static Instant endOfDay(Instant moment) {
        return endOfDay(moment.atZone(ZONE).toLocalDate());
    }

    /** True while the subscription may be used: not before {@code startsAt} (null = no start), not after {@code endsAt} (null = open-ended). */
    public static boolean effective(Instant startsAt, Instant endsAt, Instant now) {
        return (startsAt == null || !startsAt.isAfter(now)) && (endsAt == null || !now.isAfter(endsAt));
    }

    /** The contract form of an instant: ISO-8601, milliseconds, offset +05:30. Null stays null. */
    public static String format(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, OFFSET).format(CONTRACT);
    }
}
