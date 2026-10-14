package com.vyoog.eisplatform.modules.billing.model;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** REQ-BIL-002.43: a tax component of an invoice, copied at calculation time and never changed afterwards. */
@Entity
@Table(name = "invoice_tax_line")
@Getter
@Setter
public class InvoiceTaxLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "invoice_line_id")
    private Long invoiceLineId;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 14)
    private TaxTreatment treatment = TaxTreatment.TAXABLE;

    @Column(precision = 9, scale = 4)
    private BigDecimal rate;

    @Column(name = "taxable_amount", nullable = false)
    private long taxableAmount;

    @Column(name = "tax_amount", nullable = false)
    private long taxAmount;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_version")
    private Integer ruleVersion;

    @Column(length = 300)
    private String note;
}
