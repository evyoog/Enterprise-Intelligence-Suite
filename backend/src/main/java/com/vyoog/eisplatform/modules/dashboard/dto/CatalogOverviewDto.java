package com.vyoog.eisplatform.modules.dashboard.dto;

/** Platform admin dashboard (C53). {@code activeProducts} counts
 * {@code ProductStatus.ACTIVE} (published on the storefront) only. */
public record CatalogOverviewDto(
    long totalProducts,
    long activeProducts,
    long totalPlatforms
) {
}
