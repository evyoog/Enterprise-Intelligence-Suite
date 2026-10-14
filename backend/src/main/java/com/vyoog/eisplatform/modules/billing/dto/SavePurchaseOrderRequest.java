package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Pattern;

/** C97 BI-9: the customer's optional purchase-order number for an unpaid invoice; blank clears it. */
public record SavePurchaseOrderRequest(
    @Pattern(regexp = "^$|^[A-Za-z0-9][A-Za-z0-9 ./_-]{0,49}$", message = "must be up to 50 letters, digits, spaces or . / _ -") String purchaseOrderNumber
) {
}
