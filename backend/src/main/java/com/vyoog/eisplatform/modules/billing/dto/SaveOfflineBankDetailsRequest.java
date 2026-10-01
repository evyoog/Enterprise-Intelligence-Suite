package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** C55 (REQ-BIL-001.21). Field list to confirm (FRD Open question 11). */
public record SaveOfflineBankDetailsRequest(
    @NotBlank @Size(max = 200) String accountName,
    @NotBlank @Size(max = 200) String bankName,
    @NotBlank @Size(max = 34) String accountNumber,
    @Pattern(regexp = "^$|^[A-Za-z0-9]{11}$", message = "IFSC must be 11 letters or digits") String ifsc,
    @Pattern(regexp = "^$|^[A-Za-z0-9]{8}([A-Za-z0-9]{3})?$", message = "SWIFT/BIC must be 8 or 11 letters or digits") String swiftBic
) {
}
