package com.vyoog.eisplatform.modules.dashboard.dto;

/**
 * {@code platformStatus} is EIS Platform's own real Actuator health status —
 * the only service-health signal that actually exists anywhere in this
 * system. There is no monitoring of the individual products in the catalog
 * (PMS, Ticketing, etc. — see this phase's own report): nothing pings their
 * launchUrl or ingests their own status. Phase 8 (2026.3.3): also no
 * incident-tracking or status-page history anywhere in this system — this
 * is a live snapshot, not a timeline of past incidents — so {@code note}
 * says both of these explicitly rather than either gap being silently
 * papered over or a fake incident feed being invented.
 */
public record ServiceHealthDto(
    String platformStatus,
    String note
) {
}
