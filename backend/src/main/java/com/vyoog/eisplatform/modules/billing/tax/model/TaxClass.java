package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** REQ-BIL-002.19: a product or service tax classification, for example a SAC code. */
@Entity
@Table(name = "tax_class")
@Getter
@Setter
public class TaxClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "hsn_sac_code", length = 20)
    private String hsnSacCode;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
