package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveTaxClassRequest(
    @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Use letters, digits, - and _ only") String code,
    @NotBlank @Size(max = 150) String name,
    @Size(max = 20) String hsnSacCode,
    @Size(max = 500) String description,
    Boolean enabled
) {
}
