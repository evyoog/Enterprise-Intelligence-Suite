package com.vyoog.eisplatform.modules.product.service;

/**
 * Implemented by a module that owns files or rows of a product's own that must
 * go when the product is deleted (product content, REQ-CAT-004.12). Same
 * inversion as {@link ProductUsageGuard}: Spring collects the beans, so this
 * module never imports the other one.
 */
public interface ProductDeleteListener {

    /** Called inside the delete transaction, before the product row goes. */
    void onProductDeleted(Long productId);
}
