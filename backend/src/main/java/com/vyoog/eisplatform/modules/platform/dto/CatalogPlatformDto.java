package com.vyoog.eisplatform.modules.platform.dto;

import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * C66: one platform as the public Product Catalog shows it. Every value is
 * derived from stored data: {@code appCount}, {@code categories} and
 * {@code featureTags} come from the platform's ACTIVE apps. {@code apps} is
 * filled only on the detail endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogPlatformDto(
    Long id,
    String name,
    String description,
    String imageUrl,
    String primaryColor,
    int appCount,
    List<String> categories,
    List<String> featureTags,
    List<ProductDto> apps
) {
}
