package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.product.model.Currency;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** C106. Null {@code globalSeatPrice} clears the platform default (so a seat-pricing-enabled product with no
 * override of its own then has no seat add-on, until either this or the product's own price is set). */
public record SaveSeatPricingDefaultsRequest(@PositiveOrZero BigDecimal globalSeatPrice, Currency globalSeatPriceCurrency) {
}
