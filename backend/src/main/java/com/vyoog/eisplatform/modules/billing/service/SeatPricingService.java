package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * C106: the one place that resolves an application's effective per-seat
 * price — a product's own override if it has one, else the platform-wide
 * default (BillingSettings#globalSeatPrice) — so the catalog, cart, checkout,
 * subscriptions and invoicing never each compute this independently.
 * Products with {@link Product#isSeatPricingEnabled()} false are untouched by
 * this class; their total stays exactly their base price x seats, as before
 * this feature (C100 SE-1 B).
 */
@Service
@RequiredArgsConstructor
public class SeatPricingService {

    private final BillingSettingsService billingSettingsService;

    /** The resolved per-seat price, in minor units, and its currency. */
    public record SeatPrice(long unitAmount, Currency currency) {
    }

    /** Empty when the product is not seat-pricing-enabled, or neither the product nor the
     * platform has a seat price configured yet. */
    public Optional<SeatPrice> effectiveSeatPrice(Product product) {
        if (!product.isSeatPricingEnabled()) {
            return Optional.empty();
        }
        if (product.getSeatPrice() != null) {
            Currency currency = product.getSeatPriceCurrency() != null ? product.getSeatPriceCurrency() : Currency.USD;
            return Optional.of(new SeatPrice(toMinorUnits(product.getSeatPrice()), currency));
        }
        BillingSettings settings = billingSettingsService.current();
        if (settings.getGlobalSeatPrice() != null) {
            Currency currency = settings.getGlobalSeatPriceCurrency() != null ? settings.getGlobalSeatPriceCurrency() : Currency.USD;
            return Optional.of(new SeatPrice(toMinorUnits(settings.getGlobalSeatPrice()), currency));
        }
        return Optional.empty();
    }

    private long toMinorUnits(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
