package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C55 (REQ-BIL-001.21), extended by C60: offline payment details printed on
 * offline invoices and shown on the checkout's offline result — not secrets.
 * Text fields are null until a billing administrator saves them; the three
 * accepted offline methods default to enabled. */
public record OfflineBankDetailsDto(
    String accountName,
    String bankName,
    String branchName,
    String accountNumber,
    String accountType,
    String ifsc,
    String swiftBic,
    String iban,
    String micr,
    String upiId,
    String chequePayableTo,
    String chequeAddress,
    String instructions,
    boolean bankTransferEnabled,
    boolean neftRtgsEnabled,
    boolean chequeEnabled,
    Instant updatedAt
) {
    public boolean isComplete() {
        return accountName != null && bankName != null && accountNumber != null;
    }

    public static OfflineBankDetailsDto empty() {
        return new OfflineBankDetailsDto(null, null, null, null, null, null, null, null, null, null, null, null, null,
            true, true, true, null);
    }
}
