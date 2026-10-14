package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComponentDto(
    @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Use letters, digits and _ only") String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 4) BigDecimal rate,
    boolean compound
) {
}
