package com.vyoog.eisplatform.modules.product.model;

/**
 * ACTIVE shows on the public storefront (02.01.01.04 Publish product);
 * INACTIVE is admin-only (a draft/hidden state — 02.01.01.02 Update product
 * can move a product back here without retiring it). RETIRED (02.01.01.05,
 * sprint 2026.4.1) is a distinct, terminal-by-default state for a product
 * that was once published and is being withdrawn: unlike deleting it (see
 * ProductService#deleteProduct), the row and its history stay, existing
 * subscriptions and product access are left alone, but it never shows on the
 * storefront and cannot be newly subscribed to (see SubscriptionService).
 * A retired product can still be published again — see
 * ProductService#publishProduct.
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    RETIRED
}
