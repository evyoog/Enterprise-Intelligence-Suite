package com.vyoog.eisplatform.modules.support.dto;

import java.util.List;

/** C101 / REQ-SUP-002: the SLA settings API. Times of day are {@code HH:mm}. */
public final class SlaDtos {

    private SlaDtos() {
    }

    public record Policy(String priority, int firstResponseMinutes, int resolutionMinutes, boolean aroundTheClock) {
    }

    public record Settings(List<String> businessDays, String businessStart, String businessEnd, String timeZone, int warnAtPercent, List<String> alertEmails) {
    }

    public record Config(List<Policy> policies, Settings settings) {
    }

    public record PoliciesRequest(List<Policy> policies) {
    }
}
