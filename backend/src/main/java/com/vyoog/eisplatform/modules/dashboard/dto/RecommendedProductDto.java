package com.vyoog.eisplatform.modules.dashboard.dto;

/** 03.01.02 Recommendations (sprint 2027.1.2): just enough of a product to
 * show on a recommendation rail — the full catalog detail is still one
 * `GET /products/{id}` away. */
public record RecommendedProductDto(
    Long id,
    String name,
    String category,
    String imageUrl,
    String launchUrl
) {
}
