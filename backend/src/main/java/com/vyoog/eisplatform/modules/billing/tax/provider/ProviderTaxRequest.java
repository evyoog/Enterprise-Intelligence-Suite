package com.vyoog.eisplatform.modules.billing.tax.provider;

import java.time.LocalDate;
import java.util.List;

/** The vendor-neutral transaction sent to a provider (REQ-BIL-002.31 step 4). {@code transactionRef} doubles as the
 * idempotency key (BR-32). */
public record ProviderTaxRequest(
    String transactionRef,
    String transactionType,
    String companyCode,
    LocalDate date,
    String currency,
    Party supplier,
    Party customer,
    List<Line> lines
) {
    public record TaxIdentifier(String type, String number, String country, String state) {
    }

    public record Party(String name, String addressLine1, String city, String state, String postalCode, String country,
                        List<TaxIdentifier> taxIds) {
    }

    public record Line(String lineRef, String description, String taxClass, String hsnSac, long amount, int quantity) {
    }
}
