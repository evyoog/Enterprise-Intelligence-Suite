package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** REQ-BIL-002.46 (TX-6): reverses part or all of an invoice, with its tax, in proportion to the original tax lines. */
@Entity
@Table(name = "credit_note")
@Getter
@Setter
public class CreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_note_number", nullable = false, unique = true, length = 40)
    private String creditNoteNumber;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private long subtotal;

    @Column(name = "tax_amount", nullable = false)
    private long taxAmount;

    @Column(nullable = false)
    private long total;

    @Column(length = 500)
    private String reason;

    @Column(name = "tax_engine", length = 10)
    private String taxEngine;

    @Column(name = "payment_refund_id")
    private Long paymentRefundId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "issued_by", length = 100)
    private String issuedBy;
}
