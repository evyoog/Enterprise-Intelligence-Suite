package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.List;

/** "/admin/platform-dashboard" (C53) — the platform admin's own overview,
 * same honesty rule {@link BusinessDashboardDto}'s own javadoc states for
 * the organization-scoped dashboard: every figure here is a real count or
 * sum against an existing table, computed at request time. Nothing is a
 * stored snapshot and nothing is a fabricated trend. */
public record PlatformDashboardDto(
    OrganizationsOverviewDto organizations,
    CatalogOverviewDto catalog,
    SubscriptionsOverviewDto subscriptions,
    PlatformBillingOverviewDto billing,
    long openSupportTicketCount,
    long pendingReviewCount,
    ServiceHealthDto serviceHealth,
    List<TopProductDto> topProductsByLaunches
) {
}
