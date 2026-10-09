package com.vyoog.eisplatform.modules.registration.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** BR-SUB-010: the end of a subscription is 23:59:00.000 on its last day in India (+05:30, no daylight saving). */
class SubscriptionClockTest {

    @Test
    void theEndOfADayIsTwentyThreeFiftyNineInIndiaWhichIsEighteenTwentyNineUtcOnTheSameDate() {
        Instant end = SubscriptionClock.endOfDay(LocalDate.of(2026, 10, 31));

        assertThat(end).isEqualTo(Instant.parse("2026-10-31T18:29:00.000Z"));
        assertThat(SubscriptionClock.format(end)).isEqualTo("2026-10-31T23:59:00.000+05:30");
    }

    @Test
    void secondsAreZeroNotFiftyNine() {
        assertThat(SubscriptionClock.format(SubscriptionClock.endOfDay(LocalDate.of(2026, 1, 1)))).contains("T23:59:00.000+");
    }

    @Test
    void monthEndsAndLeapDaysKeepTheirCalendarDate() {
        assertThat(SubscriptionClock.format(SubscriptionClock.endOfDay(LocalDate.of(2026, 2, 28)))).isEqualTo("2026-02-28T23:59:00.000+05:30");
        assertThat(SubscriptionClock.format(SubscriptionClock.endOfDay(LocalDate.of(2028, 2, 29)))).isEqualTo("2028-02-29T23:59:00.000+05:30");
        assertThat(SubscriptionClock.format(SubscriptionClock.endOfDay(LocalDate.of(2026, 12, 31)))).isEqualTo("2026-12-31T23:59:00.000+05:30");
        assertThat(SubscriptionClock.endOfDay(LocalDate.of(2028, 2, 29)).atZone(java.time.ZoneOffset.UTC).toLocalDate())
            .as("the UTC date is the same date").isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    void aMomentIsMovedToTheEndOfItsIndianDayNotItsUtcDay() {
        // 19:00 UTC on 9 October is 00:30 on 10 October in India
        assertThat(SubscriptionClock.endOfDay(Instant.parse("2026-10-09T19:00:00Z"))).isEqualTo(Instant.parse("2026-10-10T18:29:00Z"));
        // 10:00 UTC on 9 October is 15:30 on 9 October in India
        assertThat(SubscriptionClock.endOfDay(Instant.parse("2026-10-09T10:00:00Z"))).isEqualTo(Instant.parse("2026-10-09T18:29:00Z"));
        // 18:29 UTC is 23:59 India: already the end of the day, unchanged (idempotent)
        Instant end = Instant.parse("2026-10-09T18:29:00Z");
        assertThat(SubscriptionClock.endOfDay(end)).isEqualTo(end);
        assertThat(SubscriptionClock.endOfDay(SubscriptionClock.endOfDay(Instant.parse("2026-10-09T19:00:00Z")))).isEqualTo(Instant.parse("2026-10-10T18:29:00Z"));
    }

    @Test
    void aThirtyDayTermFromAnyMomentEndsAtTheEndOfAnIndianDay() {
        Instant start = Instant.parse("2026-10-09T03:15:27.123Z");

        Instant end = SubscriptionClock.endOfDay(start.plus(java.time.Duration.ofDays(30)));

        assertThat(SubscriptionClock.format(end)).isEqualTo("2026-11-08T23:59:00.000+05:30");
    }

    @Test
    void effectiveIsInclusiveAtTheEndAndEndsAtOnceAfterIt() {
        Instant starts = Instant.parse("2025-10-31T18:30:00Z");
        Instant ends = SubscriptionClock.endOfDay(LocalDate.of(2026, 10, 31));

        assertThat(SubscriptionClock.effective(starts, ends, ends)).isTrue();
        assertThat(SubscriptionClock.effective(starts, ends, ends.plusMillis(1))).isFalse();
        assertThat(SubscriptionClock.effective(starts, ends, starts.minusMillis(1))).isFalse();
        assertThat(SubscriptionClock.effective(starts, ends, starts)).isTrue();
        assertThat(SubscriptionClock.effective(null, null, Instant.now())).as("open-ended").isTrue();
        assertThat(SubscriptionClock.effective(starts, null, Instant.parse("2099-01-01T00:00:00Z"))).isTrue();
    }

    @Test
    void aNullInstantFormatsAsNull() {
        assertThat(SubscriptionClock.format(null)).isNull();
    }
}
