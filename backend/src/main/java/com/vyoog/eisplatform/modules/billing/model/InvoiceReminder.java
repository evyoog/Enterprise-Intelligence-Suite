package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C97 BI-2: records that a payment reminder was sent for an invoice; one row per invoice and kind, so none is sent twice. */
@Entity
@Table(name = "invoice_reminder", uniqueConstraints = @UniqueConstraint(columnNames = {"invoice_id", "kind"}))
@Getter
@Setter
public class InvoiceReminder {

    public enum Kind { BEFORE_DUE, ON_DUE, AFTER_DUE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Kind kind;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();
}
