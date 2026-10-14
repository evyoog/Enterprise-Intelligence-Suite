package com.vyoog.eisplatform.modules.billing.tax.calc;

import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;

import java.time.LocalDate;
import java.util.List;

/** Everything an engine needs, gathered by the billing module before the engine runs (REQ-BIL-002.31 steps 1 to 3). An engine
 * never reads the database for the transaction itself. {@code date} is the calculation date in UTC (BR-11). Amounts are in
 * the currency's smallest unit and are WITHOUT tax (TX-1). */
public record TaxCalculationRequest(
    String transactionRef,
    TransactionType type,
    LocalDate date,
    String currency,
    Supplier supplier,
    Customer customer,
    List<Line> lines
) {
    public record Supplier(String legalName, String addressLine1, String city, String stateName, String stateKey, String postalCode,
                           String countryCode, String gstin, List<Registration> registrations) {
    }

    public record Registration(String countryCode, String stateKey, TaxType taxType, String number) {
    }

    /** {@code kind} is INDIVIDUAL or BUSINESS (BR-13). {@code countryCode} and {@code stateKey} are null when the address text
     * could not be resolved. */
    public record Customer(CustomerTypeCondition kind, Long ownerCustomerId, Long ownerOrganizationId, String name, String addressLine1,
                           String city, String rawState, String stateKey, String postalCode, String rawCountry, String countryCode,
                           String taxId) {
        public boolean hasTaxId() {
            return taxId != null && !taxId.isBlank();
        }
    }

    public record Line(String lineRef, String description, String taxClassCode, String hsnSacCode, long amount, int quantity) {
    }

    public long taxableTotal() {
        return lines.stream().mapToLong(Line::amount).sum();
    }
}
