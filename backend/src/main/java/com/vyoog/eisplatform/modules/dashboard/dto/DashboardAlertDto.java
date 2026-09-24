package com.vyoog.eisplatform.modules.dashboard.dto;

/**
 * Every alert this phase produces is DERIVED from a real, already-existing
 * fact (seat counts, subscription expiry dates, pending PAM requests, the
 * org's own MFA policy vs. this session's own amr claim) — never a
 * fabricated metric. {@code type} is a stable machine-readable code (for a
 * future notification-center to key off — see Phase 18) alongside the
 * already-composed human {@code message}.
 */
public record DashboardAlertDto(
    String type,
    String severity,
    String message
) {
}
