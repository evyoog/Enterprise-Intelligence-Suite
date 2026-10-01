package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C55 (REQ-BIL-001.21): offline bank details — not secrets. Every field is
 * null until a billing administrator saves them. */
public record OfflineBankDetailsDto(
    String accountName,
    String bankName,
    String accountNumber,
    String ifsc,
    String swiftBic,
    Instant updatedAt
) {
    public boolean isComplete() {
        return accountName != null && bankName != null && accountNumber != null;
    }
}
