package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C60: which payment methods the checkout offers, and how Razorpay Checkout
 * presents the merchant. Not secrets — Razorpay keys stay in
 * config/secrets.env (BR-SEC-001). */
public record PaymentMethodSettingsDto(
    boolean cardEnabled,
    boolean upiEnabled,
    boolean netbankingEnabled,
    boolean walletEnabled,
    boolean payByInvoiceEnabled,
    String checkoutDisplayName,
    String checkoutDescription,
    String checkoutThemeColor,
    Instant updatedAt
) {
}
