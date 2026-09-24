package com.vyoog.eisplatform.modules.product.dto;

import java.util.List;

/**
 * Always computed over the full status-scoped catalog (every category/
 * platform that exists), not re-narrowed by the current text/category/
 * platform filters already applied to {@code items} — the catalog is small
 * enough today that "every available filter, always" is simpler and more
 * useful than the query-scoped facet counts a much larger catalog would need.
 */
public record ProductSearchFacetsDto(
    List<CategoryFacet> categories,
    List<PlatformFacet> platforms
) {
}
