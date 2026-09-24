package com.vyoog.eisplatform.modules.product.dto;

import java.util.List;

public record ProductSearchResponse(
    List<ProductDto> items,
    ProductSearchFacetsDto facets
) {
}
