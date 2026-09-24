package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateRequest(
    @NotBlank String name,
    String description,
    @NotNull @Positive BigDecimal price,
    String imageUrl,
    String launchUrl,
    String category,
    // Null defaults to ACTIVE in ProductService — optional so existing callers
    // that don't send it keep working.
    ProductStatus status,
    // Admin-set — whether this app is wired into the SSO bridge. Null (e.g.
    // from an older client) defaults to false in ProductService.
    Boolean ssoConnected,
    // Which platform(s) this app should be assigned to. Null/empty means
    // "no platform" — resolved to entities in ProductService, not the mapper,
    // since that needs a repository lookup by id.
    List<Long> platformIds,
    // Empty/null means "no subscription tiers yet" — the flat price above is
    // what the storefront shows in that case. @Valid cascades validation into
    // each ProductPlanCreateRequest when this list is present.
    @Valid List<ProductPlanCreateRequest> plans
) {
}
