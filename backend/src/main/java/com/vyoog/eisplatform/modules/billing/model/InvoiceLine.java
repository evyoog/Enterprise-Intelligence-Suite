package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "invoice_line")
@Getter
@Setter
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 255)
    private String description;

    private Instant periodStart;

    private Instant periodEnd;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "unit_amount", nullable = false)
    private long unitAmount;

    @Column(nullable = false)
    private long amount;

    /** C59 (REQ-MKT-003.8): the subscription this line bills. One invoice
     * can pay a cart of several products, so each line carries its own. */
    @Column(name = "subscription_id")
    private Long subscriptionId;

    /** C96: the product's tax class and its HSN/SAC code when the line was taxed (BI-3 prints the SAC code). */
    @Column(name = "tax_class_code", length = 40)
    private String taxClassCode;

    @Column(name = "hsn_sac_code", length = 20)
    private String hsnSacCode;

    /** C96: the sum of this line's tax components. */
    @Column(name = "tax_amount", nullable = false)
    private long taxAmount = 0;
}
