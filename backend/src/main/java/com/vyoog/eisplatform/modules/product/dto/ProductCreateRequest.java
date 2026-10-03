package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Valid List<ProductPlanCreateRequest> plans,
    // 02.01.02 Product Structure (sprint 2026.4.1). Null parentProductId means
    // top-level; variantLabel is meaningless without one.
    Long parentProductId,
    String variantLabel,
    // Other products this one requires — resolved to entities in
    // ProductService (needs a repository lookup), same reason as platformIds.
    List<Long> dependsOnProductIds,
    // 03.01.02 Show featured products (sprint 2027.1.2). Null defaults to
    // false, same reason as ssoConnected above.
    Boolean featured,
    // C66 showcase fields, all optional: null accentColor = inherit the
    // platform colour; featureTags up to 12 tags of up to 40 characters.
    @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "must be a colour like #6366F1") String accentColor,
    @Size(max = 12) List<@NotBlank @Size(max = 40) String> featureTags,
    @Size(max = 500) @Pattern(regexp = "https?://.+", message = "must start with http:// or https://") String documentationUrl,
    @Size(max = 500) @Pattern(regexp = "https?://.+", message = "must start with http:// or https://") String supportUrl
) {

    /** The pre-C66 shape — showcase fields default (no colour, tags or links). */
    public ProductCreateRequest(String name, String description, BigDecimal price, String imageUrl, String launchUrl,
                                String category, ProductStatus status, Boolean ssoConnected, List<Long> platformIds,
                                List<ProductPlanCreateRequest> plans, Long parentProductId, String variantLabel,
                                List<Long> dependsOnProductIds, Boolean featured) {
        this(name, description, price, imageUrl, launchUrl, category, status, ssoConnected, platformIds, plans,
            parentProductId, variantLabel, dependsOnProductIds, featured, null, null, null, null);
    }
}
