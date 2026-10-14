package com.vyoog.eisplatform.modules.support.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

/** C101 / REQ-SUP-002: adds minutes of business time to an instant. The business hours are the working days between a start and an end time in a time zone;
 * public holidays are not modelled. Pure, so it is tested without a database. */
public final class SlaCalculator {

    private final Set<DayOfWeek> days;
    private final LocalTime start;
    private final LocalTime end;
    private final ZoneId zone;

    public SlaCalculator(Set<DayOfWeek> days, LocalTime start, LocalTime end, ZoneId zone) {
        if (days.isEmpty() || !start.isBefore(end)) {
            throw new IllegalArgumentException("Business hours need at least one day and a start before the end.");
        }
        this.days = days;
        this.start = start;
        this.end = end;
        this.zone = zone;
    }

    /** {@code from} plus {@code minutes}, counting only business time unless {@code aroundTheClock}. */
    public Instant add(Instant from, long minutes, boolean aroundTheClock) {
        if (aroundTheClock) {
            return from.plus(Duration.ofMinutes(minutes));
        }
        ZonedDateTime cursor = alignToBusinessTime(from.atZone(zone));
        long remaining = minutes;
        for (int guard = 0; guard < 5000; guard++) {
            ZonedDateTime closing = cursor.with(end);
            long available = Duration.between(cursor, closing).toMinutes();
            if (remaining <= available) {
                return cursor.plusMinutes(remaining).toInstant();
            }
            remaining -= available;
            cursor = nextBusinessDayStart(cursor.toLocalDate());
        }
        throw new IllegalStateException("Could not place the deadline within the business hours.");
    }

    /** The point of a target that has run {@code percent} of its time: used for the warning. */
    public Instant addPercent(Instant from, long minutes, int percent, boolean aroundTheClock) {
        return add(from, Math.max(1, minutes * percent / 100), aroundTheClock);
    }

    private ZonedDateTime alignToBusinessTime(ZonedDateTime t) {
        if (!days.contains(t.getDayOfWeek()) || !t.toLocalTime().isBefore(end)) {
            return nextBusinessDayStart(t.toLocalDate());
        }
        if (t.toLocalTime().isBefore(start)) {
            return t.with(start);
        }
        return t;
    }

    private ZonedDateTime nextBusinessDayStart(LocalDate after) {
        LocalDate day = after.plusDays(1);
        while (!days.contains(day.getDayOfWeek())) {
            day = day.plusDays(1);
        }
        return day.atTime(start).atZone(zone);
    }
}
