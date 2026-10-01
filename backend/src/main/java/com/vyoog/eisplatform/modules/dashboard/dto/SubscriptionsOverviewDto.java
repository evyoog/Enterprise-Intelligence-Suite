package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.Map;

/** Platform admin dashboard (C53). {@code byStatus} is keyed by
 * {@code SubscriptionStatus} name (ACTIVE, SUSPENDED, CANCELLED, EXPIRED,
 * PENDING_SUBSCRIPTION), platform-wide, across both individual and
 * organization subscriptions — real counts, one query per status. */
public record SubscriptionsOverviewDto(
    long active,
    Map<String, Long> byStatus
) {
}
