package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveBillingDetailsRequest(
    @NotBlank @Size(max = 200) String billingName,
    @NotBlank @Email @Size(max = 255) String billingEmail,
    @NotBlank @Size(max = 200) String addressLine1,
    @Size(max = 200) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Size(max = 20) String postalCode,
    @NotBlank @Size(max = 100) String country,
    @Size(max = 50) String taxId
) {
}
