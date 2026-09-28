package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotNull;

/** 09.01.01 Create/Submit order (sprint 2027.1.1), folded into one action —
 * see OrderService's own javadoc for why there is no separate draft step. */
public record SubmitOrderRequest(@NotNull Long productId, Long planId) {
}
