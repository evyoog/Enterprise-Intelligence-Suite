package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The non-secret provider settings. Credentials are never accepted here (BR-30). */
public record SaveProviderSettingsRequest(
    @NotBlank @Size(max = 40) String providerCode,
    @Size(max = 100) String companyCode,
    @NotBlank @Size(max = 12) String environment,
    @NotNull @Min(2) @Max(60) Integer timeoutSeconds,
    @NotNull @Min(1) @Max(5) Integer maxAttempts
) {
}
