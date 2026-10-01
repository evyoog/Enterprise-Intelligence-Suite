package com.vyoog.eisplatform.modules.dashboard.dto;

/** Platform admin dashboard (C53). {@code active} counts
 * {@code OrganizationLifecycleStatus.ACTIVE} only — SUSPENDED/CLOSED
 * organizations are excluded, same meaning that status already has
 * elsewhere in this codebase. */
public record OrganizationsOverviewDto(
    long total,
    long active
) {
}
