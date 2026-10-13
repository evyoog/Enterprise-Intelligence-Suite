package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** REQ-BIL-002.11: one component of a rule (CGST, SGST, IGST, a state tax …). */
@Entity
@Table(name = "tax_rule_component")
@Getter
@Setter
public class TaxRuleComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private TaxRule rule;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** Percentage, 0 to 100, up to 4 decimals (BR-24). */
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal rate;

    /** Charged on the base plus the components before it (BR-17). */
    @Column(nullable = false)
    private boolean compound = false;
}
