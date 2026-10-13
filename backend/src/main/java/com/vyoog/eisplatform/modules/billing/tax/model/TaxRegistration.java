package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** REQ-BIL-002.37: one of the supplier's tax registrations, sent to an external provider. */
@Entity
@Table(name = "tax_registration")
@Getter
@Setter
public class TaxRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "state_code", length = 20)
    private String stateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 12)
    private TaxType taxType;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public boolean isInForce(LocalDate date) {
        return enabled && (effectiveFrom == null || !effectiveFrom.isAfter(date)) && (effectiveTo == null || !effectiveTo.isBefore(date));
    }
}
