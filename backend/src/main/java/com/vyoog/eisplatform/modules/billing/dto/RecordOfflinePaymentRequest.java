package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.OfflinePaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** C55 (REQ-BIL-001.20). {@code amount} is in the currency's smallest unit
 * and must equal the invoice's open amount. */
public record RecordOfflinePaymentRequest(
    @Positive long amount,
    @NotNull LocalDate receivedOn,
    @NotNull OfflinePaymentMethod method,
    @NotBlank @Size(max = 100) String reference,
    @Size(max = 500) String note
) {
}
