package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** REQ-BIL-002.10 to .14: one version of a tax rule family ({@code ruleKey}). Only APPROVED, active rows whose dates cover
 * the invoice date are used by the internal engine (BR-11). Rates are never defaulted anywhere in code (BR-10). */
@Entity
@Table(name = "tax_rule")
@Getter
@Setter
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", nullable = false, length = 36)
    private String ruleKey;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /** Normalised state key (see StateCatalog); null = the rule applies to the whole country. */
    @Column(name = "state_code", length = 20)
    private String stateCode;

    @Column(length = 150)
    private String jurisdiction;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 12)
    private TaxType taxType;

    @Column(name = "product_class_code", length = 40)
    private String productClassCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 12)
    private CustomerTypeCondition customerType = CustomerTypeCondition.ANY;

    @Enumerated(EnumType.STRING)
    @Column(name = "supply_type", nullable = false, length = 14)
    private SupplyType supplyType = SupplyType.ANY;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_id_condition", nullable = false, length = 12)
    private TaxIdCondition taxIdCondition = TaxIdCondition.ANY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 14)
    private TaxTreatment treatment = TaxTreatment.TAXABLE;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 18)
    private RuleApprovalStatus approvalStatus = RuleApprovalStatus.DRAFT;

    @Column(nullable = false)
    private boolean active = false;

    @Column(length = 500)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_edited_by", length = 100)
    private String lastEditedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC, id ASC")
    private List<TaxRuleComponent> components = new ArrayList<>();

    public boolean isInForce(LocalDate date) {
        return approvalStatus == RuleApprovalStatus.APPROVED && active
            && !effectiveFrom.isAfter(date) && (effectiveTo == null || !effectiveTo.isBefore(date));
    }

    public boolean isEditable() {
        return approvalStatus == RuleApprovalStatus.DRAFT || approvalStatus == RuleApprovalStatus.REJECTED;
    }

    /** BR-15 specificity: state 16, tax class 8, supply type 4, customer type 2, tax ID condition 1. */
    public int specificity() {
        return (stateCode != null ? 16 : 0) + (productClassCode != null ? 8 : 0) + (supplyType != SupplyType.ANY ? 4 : 0)
            + (customerType != CustomerTypeCondition.ANY ? 2 : 0) + (taxIdCondition != TaxIdCondition.ANY ? 1 : 0);
    }

    public List<TaxRuleComponent> orderedComponents() {
        return components.stream().sorted(Comparator.comparingInt(TaxRuleComponent::getSortOrder)).toList();
    }
}
