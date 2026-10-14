package com.vyoog.eisplatform.modules.support.service;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C101 / REQ-SUP-002: business-hours arithmetic (Mon to Fri 09:00 to 18:00, Asia/Kolkata). 2026-10-12 is a Monday. */
class SlaCalculatorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private final SlaCalculator calc = new SlaCalculator(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), LocalTime.of(9, 0), LocalTime.of(18, 0), ZONE);

    private static Instant at(int day, int hour, int minute) {
        return LocalDateTime.of(2026, 10, day, hour, minute).atZone(ZONE).toInstant();
    }

    @Test
    void withinTheSameBusinessDay() {
        assertThat(calc.add(at(12, 10, 0), 240, false)).isEqualTo(at(12, 14, 0));
    }

    @Test
    void spillsOverTheEndOfTheDayAndTheWeekend() {
        // Friday 16:00 + 4 h: two hours left on Friday, two on Monday morning.
        assertThat(calc.add(at(16, 16, 0), 240, false)).isEqualTo(at(19, 11, 0));
    }

    @Test
    void aTicketCreatedOutsideBusinessHoursStartsAtTheNextOpening() {
        assertThat(calc.add(at(17, 12, 0), 60, false)).isEqualTo(at(19, 10, 0));   // Saturday noon
        assertThat(calc.add(at(12, 7, 0), 60, false)).isEqualTo(at(12, 10, 0));    // before opening
        assertThat(calc.add(at(12, 18, 0), 60, false)).isEqualTo(at(13, 10, 0));   // at closing time
    }

    @Test
    void severalDays() {
        // 1440 min = two 9 h days (1080) + 6 h: Monday 09:00 -> Wednesday 15:00.
        assertThat(calc.add(at(12, 9, 0), 1440, false)).isEqualTo(at(14, 15, 0));
    }

    @Test
    void aroundTheClockIgnoresBusinessHours() {
        assertThat(calc.add(at(17, 23, 30), 60, true)).isEqualTo(at(18, 0, 30));
    }

    @Test
    void theWarningPointIsAPercentageOfTheTarget() {
        assertThat(calc.addPercent(at(12, 10, 0), 300, 80, false)).isEqualTo(at(12, 14, 0));
        assertThat(calc.addPercent(at(12, 10, 0), 60, 80, true)).isEqualTo(at(12, 10, 48));
    }

    @Test
    void businessHoursNeedADayAndAStartBeforeTheEnd() {
        assertThatThrownBy(() -> new SlaCalculator(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(9, 0), LocalTime.of(18, 0), ZONE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlaCalculator(EnumSet.of(DayOfWeek.MONDAY), LocalTime.of(18, 0), LocalTime.of(9, 0), ZONE)).isInstanceOf(IllegalArgumentException.class);
    }
}
