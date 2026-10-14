package com.vyoog.eisplatform.modules.registration.dto;

import java.util.List;

/** 09.01.01 Create/Submit order (sprint 2027.1.1), folded into one action. C100 CA-1 B: either one product ({@code productId}, {@code planId}) as before,
 * or {@code items} (several products in one order). C100 CA-2: {@code reason} is why the member asks for the purchase (up to 500 characters). */
public record SubmitOrderRequest(Long productId, Long planId, List<Item> items, String reason) {

    public record Item(Long productId, Long planId) {
    }

    public SubmitOrderRequest(Long productId, Long planId) {
        this(productId, planId, null, null);
    }
}
