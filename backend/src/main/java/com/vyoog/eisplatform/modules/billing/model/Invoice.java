package com.vyoog.eisplatform.modules.billing.model;

import com.vyoog.eisplatform.modules.product.model.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Belongs to EXACTLY ONE of an individual customer or an organization,
 * same ownership shape as {@code ProductSubscription}. Amounts are stored in
 * the currency's smallest unit (BR-2, e.g. paise) as {@code long}, not
 * {@code BigDecimal} — deliberately different from {@code ProductPlan.price}
 * (display/catalog price), since billing arithmetic (refund remainders,
 * partial refunds) is exact only in integer minor units.
 *
 * <p>C96 (REQ-BIL-002): {@link #taxAmount} comes from the engine chosen on Tax Configuration
 * (internal rules or external provider), recorded in {@link #taxEngine} and {@link #taxConfigVersion}. When tax cannot be
 * determined the invoice is a {@link InvoiceStatus#DRAFT} with {@link #taxStatus} BLOCKED and the reason; it gets its
 * number, issue date and due date only when it is finalized (BR-40). Before C96 every invoice had tax 0 and tax status
 * LEGACY. */
@Entity
@Table(name = "invoice")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Immutable once finalized (BR-3). Format: {@code INV-<year>-<id, zero-padded to 6>}
     * — decided in C47 since the FRD left the format Not specified. */
    @Column(name = "invoice_number", unique = true, length = 40)
    private String invoiceNumber;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(nullable = false)
    private long subtotal;

    @Column(name = "tax_amount", nullable = false)
    private long taxAmount = 0;

    @Column(nullable = false)
    private long total;

    private Instant periodStart;

    private Instant periodEnd;

    /** Set when the invoice is created and again when a Draft is finalized (the invoice date is the finalization date). */
    @CreatedDate
    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    /** Billing details copied at issue time, so a later edit to Billing
     * details never changes an already-issued invoice's bill-to. Free text
     * (name / address rendered as one block) rather than a second FK'd
     * snapshot table — this is display-only, never re-parsed. */
    @Column(name = "bill_to_snapshot", length = 1000)
    private String billToSnapshot;

    /** C55 (REQ-BIL-001.19): the route the customer chose at checkout —
     * null until a route is chosen. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_route", length = 10)
    private PaymentRoute paymentRoute;

    // ---- C96: tax ----
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_engine", length = 10)
    private com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType taxEngine;

    @Column(name = "tax_config_version")
    private Integer taxConfigVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_status", nullable = false, length = 12)
    private com.vyoog.eisplatform.modules.billing.tax.model.InvoiceTaxStatus taxStatus
        = com.vyoog.eisplatform.modules.billing.tax.model.InvoiceTaxStatus.LEGACY;

    @Column(name = "tax_block_code", length = 40)
    private String taxBlockCode;

    @Column(name = "tax_block_reason", length = 500)
    private String taxBlockReason;

    @Column(name = "tax_calculation_id")
    private Long taxCalculationId;

    /** BI-3: the state (or country) the supply is made to, printed on the invoice. */
    @Column(name = "place_of_supply", length = 100)
    private String placeOfSupply;

    /** BI-3: the issuer's legal name, address, GSTIN and PAN as they were when the invoice was finalized. */
    @Column(name = "issuer_snapshot", length = 1500)
    private String issuerSnapshot;

    /** BI-3: the customer's tax ID (for example GSTIN) as it was when the invoice was finalized. */
    @Column(name = "customer_tax_id", length = 50)
    private String customerTaxId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceLine> lines = new ArrayList<>();
}
