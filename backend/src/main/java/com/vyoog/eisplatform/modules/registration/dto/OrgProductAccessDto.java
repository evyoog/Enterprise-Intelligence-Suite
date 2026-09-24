package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

/**
 * The two-tier view the Product Suite needs for an organization member: the
 * ORG's subscription state (null = the org never purchased this product) is
 * deliberately a separate field from this specific member's own assignment
 * ({@code myAccessAssigned}/{@code myProductRole}) — the org owning a
 * subscription never implies a member can use it; a row in
 * organization_product_access is what actually grants that.
 */
public record OrgProductAccessDto(
    Long productId,
    String productName,
    String category,
    SubscriptionStatus orgSubscriptionStatus,
    boolean myAccessAssigned,
    String myProductRole
) {
}
