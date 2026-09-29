package com.vyoog.eisplatform.modules.administration.service;

import java.util.Optional;

/**
 * Implemented by any module that can hold real data referencing a region
 * (today: an Organization's 05.02.01.03 region assignment) — same pattern as
 * {@code com.vyoog.eisplatform.modules.product.service.ProductUsageGuard}.
 * Spring collects every bean implementing this, so this module never needs
 * to know who they are.
 */
public interface PlatformRegionUsageGuard {

    /** A short, human-readable reason this region can't be deleted, or empty
     * if this guard has no objection. */
    Optional<String> blockingReason(Long regionId);
}
