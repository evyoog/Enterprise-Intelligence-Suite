package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SavePaymentMethodSettingsRequest(
    boolean cardEnabled,
    boolean upiEnabled,
    boolean netbankingEnabled,
    boolean walletEnabled,
    boolean payByInvoiceEnabled,
    @Size(max = 100) String checkoutDisplayName,
    @Size(max = 255) String checkoutDescription,
    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "must be a colour like #4C63FF") String checkoutThemeColor
) {
}
