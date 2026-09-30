package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RefundRequest(
    @Positive long amount,
    @NotBlank @Size(max = 500) String reason
) {
}
