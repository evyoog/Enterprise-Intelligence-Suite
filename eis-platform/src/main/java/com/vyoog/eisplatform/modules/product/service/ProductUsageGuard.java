package com.vyoog.eisplatform.modules.product.service;

import java.util.Optional;

/**
 * Implemented by any module that can hold real, non-catalog data referencing
 * a product (subscriptions, org access grants, favorites, usage history) —
 * lets {@link ProductService} ask "is this product safe to hard-delete"
 * without importing those modules' repositories. Those modules already
 * depend on this one (see LayeredArchitectureTest's whitelist for
 * ..modules.product..); a plain import in the other direction would make
 * that a cycle, so Spring collects every {@code ProductUsageGuard} bean
 * instead and this module never needs to know who they are.
 */
public interface ProductUsageGuard {

    /** A short, human-readable reason this product can't be deleted (e.g.
     * "an existing subscription"), or empty if this guard has no objection. */
    Optional<String> blockingReason(Long productId);
}
