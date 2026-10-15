package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.product.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

/** C106: the platform-wide default per-seat price, used by any application that is seat-pricing-enabled
 * and has not set its own override. Null {@code globalSeatPrice} means no default is configured yet. */
public record SeatPricingDefaultsDto(BigDecimal globalSeatPrice, Currency globalSeatPriceCurrency, Instant updatedAt) {
}
