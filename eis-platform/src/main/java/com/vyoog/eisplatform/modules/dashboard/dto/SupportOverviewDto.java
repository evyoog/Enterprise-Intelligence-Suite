package com.vyoog.eisplatform.modules.dashboard.dto;

/**
 * {@code available} is always false today — the separate Ticketing app has
 * no endpoint to query tickets by customer/organization (its API is
 * board/admin-scoped, role-gated to ticket-desk staff), so there is nothing
 * real to surface here yet. {@code note} explains the actual blocker rather
 * than the section silently being empty.
 */
public record SupportOverviewDto(
    boolean available,
    String note
) {
}
