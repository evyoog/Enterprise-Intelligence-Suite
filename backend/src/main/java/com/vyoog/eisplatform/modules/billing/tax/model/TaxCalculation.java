package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** BR-2, BR-35: one calculation attempt on an invoice, with the engine and configuration version pinned, the provider
 * reference and a copy of the validated response (never a credential). */
@Entity
@Table(name = "tax_calculation")
@Getter
@Setter
public class TaxCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "transaction_ref", nullable = false, length = 80)
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 12)
    private TransactionType transactionType = TransactionType.SALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaxEngineType engine;

    @Column(name = "config_version", nullable = false)
    private int configVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TaxCalculationStatus status;

    @Column(name = "reason_code", length = 40)
    private String reasonCode;

    @Column(name = "reason_message", length = 500)
    private String reasonMessage;

    @Column(name = "provider_code", length = 40)
    private String providerCode;

    @Column(name = "provider_reference", length = 120)
    private String providerReference;

    @Column(nullable = false)
    private int attempts = 1;

    @Column(length = 10)
    private String currency;

    @Column(name = "taxable_total")
    private Long taxableTotal;

    @Column(name = "tax_total")
    private Long taxTotal;

    @Column(name = "response_copy", columnDefinition = "TEXT")
    private String responseCopy;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();
}
