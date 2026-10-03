package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** C55 (REQ-BIL-001.21), extended by C60. Formats only (IFSC, SWIFT/BIC,
 * IBAN, MICR, UPI ID); no lookup against a bank directory. */
public record SaveOfflineBankDetailsRequest(
    @NotBlank @Size(max = 200) String accountName,
    @NotBlank @Size(max = 200) String bankName,
    @Size(max = 200) String branchName,
    @NotBlank @Size(max = 34) String accountNumber,
    @Pattern(regexp = "^$|CURRENT|SAVINGS", message = "must be CURRENT or SAVINGS") String accountType,
    @Pattern(regexp = "(?i)^$|^[A-Z]{4}0[A-Z0-9]{6}$", message = "IFSC must be 11 characters: 4 letters, 0, then 6 letters or digits") String ifsc,
    @Pattern(regexp = "(?i)^$|^[A-Z0-9]{8}([A-Z0-9]{3})?$", message = "SWIFT/BIC must be 8 or 11 letters or digits") String swiftBic,
    @Pattern(regexp = "(?i)^$|^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$", message = "IBAN must start with a country code and check digits") String iban,
    @Pattern(regexp = "^$|^[0-9]{9}$", message = "MICR must be 9 digits") String micr,
    @Pattern(regexp = "^$|^[A-Za-z0-9._-]{2,256}@[A-Za-z]{2,64}$", message = "UPI ID must look like name@bank") String upiId,
    @Size(max = 200) String chequePayableTo,
    @Size(max = 500) String chequeAddress,
    @Size(max = 1000) String instructions,
    Boolean bankTransferEnabled,
    Boolean neftRtgsEnabled,
    Boolean chequeEnabled
) {
}
