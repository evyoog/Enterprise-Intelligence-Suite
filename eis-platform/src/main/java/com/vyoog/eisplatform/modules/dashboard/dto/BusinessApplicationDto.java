package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

import java.time.Instant;

/**
 * One product from the organization's own point of view — "usage" here is a
 * real org-wide aggregate (every member's own {@code ProductUsage} row for
 * this product, summed/maxed), not a per-customer figure the way
 * DashboardProductDto's is. {@code assignedMembers} is a real count of
 * ACTIVE OrganizationProductAccess rows, not a fabricated adoption metric.
 */
public record BusinessApplicationDto(
    Long productId,
    String productName,
    String category,
    SubscriptionStatus subscriptionStatus,
    long assignedMembers,
    long totalLaunches,
    Instant lastUsedAt
) {
}
