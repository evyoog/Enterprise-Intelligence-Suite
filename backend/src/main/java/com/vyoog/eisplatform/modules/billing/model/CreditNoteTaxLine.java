package com.vyoog.eisplatform.modules.billing.model;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_note_tax_line")
@Getter
@Setter
public class CreditNoteTaxLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_note_id", nullable = false)
    private Long creditNoteId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "component_code", nullable = false, length = 30)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", length = 12)
    private TaxType taxType;

    @Column(length = 150)
    private String jurisdiction;

    @Column(precision = 9, scale = 4)
    private BigDecimal rate;

    @Column(name = "taxable_amount", nullable = false)
    private long taxableAmount;

    @Column(name = "tax_amount", nullable = false)
    private long taxAmount;
}
