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
 * <p>Tax is not calculated in this pass (decision C47, FRD Open question 1:
 * D4 tax rule not yet decided) — {@link #taxAmount} is always zero and
 * exists only so the column/API shape doesn't need to change once a real
 * tax rule is decided. Invoice numbers are due immediately (no net terms —
 * FRD Open question 4 payment terms not yet decided): {@link #dueAt} equals
 * {@link #issuedAt}. */
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

    @CreatedDate
    @Column(name = "issued_at", updatable = false)
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

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceLine> lines = new ArrayList<>();
}
