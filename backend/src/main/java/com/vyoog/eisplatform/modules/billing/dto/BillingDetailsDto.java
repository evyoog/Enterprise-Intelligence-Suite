package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

public record BillingDetailsDto(
    Long id,
    String billingName,
    String billingEmail,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country,
    String taxId,
    Instant updatedAt
) {
}
