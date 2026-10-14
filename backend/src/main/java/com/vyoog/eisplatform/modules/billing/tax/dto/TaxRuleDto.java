package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TaxRuleDto(
    Long id,
    String ruleKey,
    int version,
    boolean latestVersion,
    String name,
    String countryCode,
    String countryName,
    String stateCode,
    String stateName,
    String jurisdiction,
    TaxType taxType,
    String productClassCode,
    CustomerTypeCondition customerType,
    SupplyType supplyType,
    TaxIdCondition taxIdCondition,
    TaxTreatment treatment,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    RuleApprovalStatus approvalStatus,
    boolean active,
    boolean inForce,
    String notes,
    String rejectionReason,
    Instant createdAt,
    String createdBy,
    String lastEditedBy,
    Instant approvedAt,
    String approvedBy,
    List<ComponentDto> components,
    BigDecimal totalRate
) {
}
