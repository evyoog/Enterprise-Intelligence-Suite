package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

import java.time.Instant;

/**
 * One product entry in the dashboard's product list — deliberately a single
 * shape for both individual and organization callers, rather than the two
 * separate {@code MyProductDto}/{@code OrgProductAccessDto} shapes those
 * simpler, older endpoints use — so the frontend can render one list either
 * way. {@code myAccessAssigned}/{@code myProductRole} are null for an
 * individual caller (the concept doesn't apply — see OrganizationProductAccess's
 * own javadoc on why entitlement tracking is an organization-only concept).
 * {@code favorite}/{@code lastLaunchedAt}/{@code launchCount} are the real,
 * per-customer personalization data this phase adds — the frontend derives
 * "favorites" (filter where favorite) and "recently used" (sort by
 * lastLaunchedAt) from this SAME list rather than needing separate ones.
 */
public record DashboardProductDto(
    Long productId,
    String productName,
    String category,
    String launchUrl,
    SubscriptionStatus subscriptionStatus,
    Boolean myAccessAssigned,
    String myProductRole,
    boolean favorite,
    Instant lastLaunchedAt,
    long launchCount
) {
}
