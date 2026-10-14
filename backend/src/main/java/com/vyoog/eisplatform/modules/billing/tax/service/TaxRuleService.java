package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.dto.ComponentDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRuleRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxRuleDto;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRuleComponent;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxClassRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** REQ-BIL-002.10 to .14, .21: tax rules, their versions and their approval. A rule is edited only while Draft or Rejected;
 * an approved rule gets a new version instead (BR-20, BR-21). The approver is a different administrator unless the platform
 * setting allows otherwise (BR-22). */
@Service
@RequiredArgsConstructor
public class TaxRuleService {

    private final TaxRuleRepository ruleRepository;
    private final TaxClassRepository classRepository;
    private final TaxSettingsService settingsService;
    private final TaxRequestAssembler assembler;
    private final InternalTaxEngine internalEngine;
    private final TaxActorLabels actorLabels;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TaxRuleDto> list() {
        List<TaxRule> all = ruleRepository.findAllByOrderByCountryCodeAscNameAscVersionDesc();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return all.stream().map(r -> toDto(r, today, latestVersionOf(all, r.getRuleKey()))).toList();
    }

    @Transactional(readOnly = true)
    public TaxRuleDto get(Long id) {
        TaxRule r = find(id);
        return toDto(r, LocalDate.now(ZoneOffset.UTC), ruleRepository.findFirstByRuleKeyOrderByVersionDesc(r.getRuleKey()).map(TaxRule::getVersion).orElse(r.getVersion()));
    }

    @Transactional
    public TaxRuleDto create(String actorSub, SaveTaxRuleRequest r) {
        TaxRule rule = new TaxRule();
        rule.setRuleKey(UUID.randomUUID().toString());
        rule.setVersion(1);
        rule.setCreatedBy(actorSub);
        rule.setLastEditedBy(actorSub);
        apply(rule, r);
        rule = ruleRepository.save(rule);
        audit(actorSub, "TAX_RULE_CREATED", rule, "Tax rule \"" + rule.getName() + "\" v1 created (draft)");
        return get(rule.getId());
    }

    @Transactional
    public TaxRuleDto update(String actorSub, Long id, SaveTaxRuleRequest r) {
        TaxRule rule = find(id);
        requireEditable(rule);
        apply(rule, r);
        rule.setLastEditedBy(actorSub);
        rule.setRejectionReason(null);
        rule.setApprovalStatus(RuleApprovalStatus.DRAFT);
        ruleRepository.save(rule);
        audit(actorSub, "TAX_RULE_UPDATED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " edited");
        return get(id);
    }

    @Transactional
    public void delete(String actorSub, Long id) {
        TaxRule rule = find(id);
        requireEditable(rule);
        ruleRepository.delete(rule);
        audit(actorSub, "TAX_RULE_DELETED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " deleted");
    }

    @Transactional
    public TaxRuleDto submit(String actorSub, Long id) {
        TaxRule rule = find(id);
        requireEditable(rule);
        validateComplete(rule);
        rule.setApprovalStatus(RuleApprovalStatus.PENDING_APPROVAL);
        rule.setSubmittedAt(Instant.now());
        rule.setLastEditedBy(actorSub);
        rule.setRejectionReason(null);
        ruleRepository.save(rule);
        audit(actorSub, "TAX_RULE_SUBMITTED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " submitted for approval");
        return get(id);
    }

    @Transactional
    public TaxRuleDto approve(String actorSub, Long id) {
        TaxRule rule = find(id);
        if (rule.getApprovalStatus() != RuleApprovalStatus.PENDING_APPROVAL) {
            throw new BillingConflictException("Only a rule that is pending approval can be approved.");
        }
        requireDifferentApprover(actorSub, rule);
        validateComplete(rule);
        requireNoOverlap(rule);
        // BR-21: approving a new version closes the earlier versions the day before.
        for (TaxRule earlier : ruleRepository.findByRuleKeyOrderByVersionDesc(rule.getRuleKey())) {
            if (earlier.getVersion() >= rule.getVersion() || earlier.getApprovalStatus() != RuleApprovalStatus.APPROVED) {
                continue;
            }
            if (!rule.getEffectiveFrom().isAfter(earlier.getEffectiveFrom())) {
                throw new BillingConflictException("A new version must start after version " + earlier.getVersion() + " started ("
                    + earlier.getEffectiveFrom() + ").");
            }
            if (earlier.getEffectiveTo() == null || !earlier.getEffectiveTo().isBefore(rule.getEffectiveFrom())) {
                earlier.setEffectiveTo(rule.getEffectiveFrom().minusDays(1));
                earlier.setUpdatedAt(Instant.now());
                ruleRepository.save(earlier);
            }
        }
        rule.setApprovalStatus(RuleApprovalStatus.APPROVED);
        rule.setActive(true);
        rule.setApprovedBy(actorSub);
        rule.setApprovedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);
        audit(actorSub, "TAX_RULE_APPROVED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " approved and activated");
        return get(id);
    }

    @Transactional
    public TaxRuleDto reject(String actorSub, Long id, String reason) {
        TaxRule rule = find(id);
        if (rule.getApprovalStatus() != RuleApprovalStatus.PENDING_APPROVAL) {
            throw new BillingConflictException("Only a rule that is pending approval can be rejected.");
        }
        requireDifferentApprover(actorSub, rule);
        rule.setApprovalStatus(RuleApprovalStatus.REJECTED);
        rule.setRejectionReason(reason.trim());
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);
        audit(actorSub, "TAX_RULE_REJECTED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " rejected: " + reason.trim());
        return get(id);
    }

    @Transactional
    public TaxRuleDto activate(String actorSub, Long id) {
        TaxRule rule = find(id);
        if (rule.getApprovalStatus() != RuleApprovalStatus.APPROVED) {
            throw new BillingConflictException("Only an approved rule can be activated.");
        }
        if (!rule.isActive()) {
            requireNoOverlap(rule);
            rule.setActive(true);
            rule.setUpdatedAt(Instant.now());
            ruleRepository.save(rule);
            audit(actorSub, "TAX_RULE_ACTIVATED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " activated");
        }
        return get(id);
    }

    @Transactional
    public TaxRuleDto deactivate(String actorSub, Long id) {
        TaxRule rule = find(id);
        if (rule.getApprovalStatus() != RuleApprovalStatus.APPROVED) {
            throw new BillingConflictException("Only an approved rule can be deactivated.");
        }
        if (rule.isActive()) {
            rule.setActive(false);
            rule.setUpdatedAt(Instant.now());
            ruleRepository.save(rule);
            audit(actorSub, "TAX_RULE_DEACTIVATED", rule, "Tax rule \"" + rule.getName() + "\" v" + rule.getVersion() + " deactivated");
        }
        return get(id);
    }

    /** BR-21: copies an approved rule into a Draft with the next version number. */
    @Transactional
    public TaxRuleDto newVersion(String actorSub, Long id) {
        TaxRule source = find(id);
        if (source.getApprovalStatus() != RuleApprovalStatus.APPROVED) {
            throw new BillingConflictException("Only an approved rule can have a new version.");
        }
        List<TaxRule> versions = ruleRepository.findByRuleKeyOrderByVersionDesc(source.getRuleKey());
        TaxRule latest = versions.get(0);
        if (latest.getApprovalStatus() == RuleApprovalStatus.DRAFT || latest.getApprovalStatus() == RuleApprovalStatus.PENDING_APPROVAL
                || latest.getApprovalStatus() == RuleApprovalStatus.REJECTED) {
            throw new BillingConflictException("Version " + latest.getVersion() + " of this rule is still " + latest.getApprovalStatus().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                + ". Finish or delete it first.");
        }
        TaxRule copy = new TaxRule();
        copy.setRuleKey(source.getRuleKey());
        copy.setVersion(latest.getVersion() + 1);
        copy.setName(source.getName());
        copy.setCountryCode(source.getCountryCode());
        copy.setStateCode(source.getStateCode());
        copy.setJurisdiction(source.getJurisdiction());
        copy.setTaxType(source.getTaxType());
        copy.setProductClassCode(source.getProductClassCode());
        copy.setCustomerType(source.getCustomerType());
        copy.setSupplyType(source.getSupplyType());
        copy.setTaxIdCondition(source.getTaxIdCondition());
        copy.setTreatment(source.getTreatment());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        copy.setEffectiveFrom(today.isAfter(source.getEffectiveFrom()) ? today : source.getEffectiveFrom().plusDays(1));
        copy.setNotes(source.getNotes());
        copy.setCreatedBy(actorSub);
        copy.setLastEditedBy(actorSub);
        for (TaxRuleComponent c : source.orderedComponents()) {
            TaxRuleComponent cc = new TaxRuleComponent();
            cc.setRule(copy);
            cc.setSortOrder(c.getSortOrder());
            cc.setCode(c.getCode());
            cc.setName(c.getName());
            cc.setRate(c.getRate());
            cc.setCompound(c.isCompound());
            copy.getComponents().add(cc);
        }
        copy = ruleRepository.save(copy);
        audit(actorSub, "TAX_RULE_VERSIONED", copy, "Tax rule \"" + copy.getName() + "\" version " + copy.getVersion() + " created as a draft from version " + source.getVersion());
        return get(copy.getId());
    }

    /** REQ-BIL-002.21: the internal rules applied to a made-up transaction. Creates nothing. */
    @Transactional(readOnly = true)
    public TaxPreviewDto preview(TaxPreviewRequest r) {
        LocalDate date = r.date() == null ? LocalDate.now(ZoneOffset.UTC) : r.date();
        TaxCalculationRequest.Customer base = assembler.customer(null, null);
        String country = CountryResolver.resolve(r.country()).orElse(null);
        String stateKey = country == null ? null : StateCatalog.normalize(country, r.state()).orElse(null);
        TaxCalculationRequest.Customer customer = new TaxCalculationRequest.Customer(
            r.customerType() == null || r.customerType() == CustomerTypeCondition.ANY ? CustomerTypeCondition.INDIVIDUAL : r.customerType(),
            null, null, "Preview", null, null, r.state(), stateKey, null, r.country(), country, r.taxId());
        String classCode = r.productClassCode() == null || r.productClassCode().isBlank() ? null : r.productClassCode().trim().toUpperCase(Locale.ROOT);
        TaxCalculationRequest request = new TaxCalculationRequest("PREVIEW", TransactionType.SALE, date, r.currency().trim().toUpperCase(Locale.ROOT),
            assembler.supplier(date), customer, List.of(new TaxCalculationRequest.Line("1", "Preview", classCode, null, r.amount(), 1)));
        TaxCalculationResult result = internalEngine.calculate(request, settingsService.snapshot());
        if (!result.isCalculated()) {
            return new TaxPreviewDto("BLOCKED", result.blockCode().name(), result.blockMessage(), null, r.amount(), 0, r.amount(), List.of());
        }
        List<TaxPreviewDto.Component> components = result.lines().stream().flatMap(l -> l.components().stream())
            .map(c -> new TaxPreviewDto.Component(c.code(), c.name(), c.taxType() == null ? null : c.taxType().name(), c.jurisdiction(),
                c.treatment().name(), c.rate(), c.taxableAmount(), c.taxAmount(), c.ruleId(), c.ruleVersion(), c.note())).toList();
        return new TaxPreviewDto("CALCULATED", null, null, result.placeOfSupply(), r.amount(), result.totalTax(), r.amount() + result.totalTax(), components);
    }

    // ---- helpers ----

    private TaxRule find(Long id) {
        return ruleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax rule not found"));
    }

    private void requireEditable(TaxRule rule) {
        if (!rule.isEditable()) {
            throw new BillingConflictException("Only a draft or rejected rule can be changed. Create a new version of an approved rule instead.");
        }
    }

    /** BR-22. */
    private void requireDifferentApprover(String actorSub, TaxRule rule) {
        if (settingsService.current().isAllowSelfApproval()) {
            return;
        }
        if (actorSub != null && (actorSub.equals(rule.getLastEditedBy()) || actorSub.equals(rule.getCreatedBy()) && rule.getLastEditedBy() == null)) {
            throw new BillingConflictException("A different administrator must approve this rule: you edited or submitted it. "
                + "(Tax Configuration, Approval policy, can allow the same administrator.)");
        }
    }

    /** BR-23. */
    private void requireNoOverlap(TaxRule rule) {
        for (TaxRule other : ruleRepository.findByCountryCodeAndApprovalStatus(rule.getCountryCode(), RuleApprovalStatus.APPROVED)) {
            if (other.getId().equals(rule.getId()) || other.getRuleKey().equals(rule.getRuleKey()) || !other.isActive()) {
                continue;
            }
            boolean sameScope = Objects.equals(other.getStateCode(), rule.getStateCode())
                && Objects.equals(other.getProductClassCode(), rule.getProductClassCode())
                && other.getCustomerType() == rule.getCustomerType() && other.getSupplyType() == rule.getSupplyType()
                && other.getTaxIdCondition() == rule.getTaxIdCondition();
            if (sameScope && overlaps(other, rule)) {
                throw new BillingConflictException("\"" + other.getName() + "\" (v" + other.getVersion() + ") is already approved and active for the same scope "
                    + "and overlapping dates. Change a date or deactivate it first.");
            }
        }
    }

    private static boolean overlaps(TaxRule a, TaxRule b) {
        LocalDate aEnd = a.getEffectiveTo() == null ? LocalDate.MAX : a.getEffectiveTo();
        LocalDate bEnd = b.getEffectiveTo() == null ? LocalDate.MAX : b.getEffectiveTo();
        return !a.getEffectiveFrom().isAfter(bEnd) && !b.getEffectiveFrom().isAfter(aEnd);
    }

    private void apply(TaxRule rule, SaveTaxRuleRequest r) {
        String country = r.countryCode().trim().toUpperCase(Locale.ROOT);
        if (!CountryResolver.isKnownCode(country)) {
            throw new IllegalArgumentException("Unknown country code: " + country);
        }
        String state = null;
        if (r.stateCode() != null && !r.stateCode().isBlank()) {
            state = StateCatalog.normalize(country, r.stateCode())
                .orElseThrow(() -> new IllegalArgumentException("\"" + r.stateCode() + "\" is not a known state of " + CountryResolver.nameOf(country) + "."));
        }
        if (r.effectiveTo() != null && r.effectiveTo().isBefore(r.effectiveFrom())) {
            throw new IllegalArgumentException("The end date cannot be before the start date.");
        }
        String classCode = null;
        if (r.productClassCode() != null && !r.productClassCode().isBlank()) {
            classCode = r.productClassCode().trim().toUpperCase(Locale.ROOT);
            if (!classRepository.existsByCode(classCode)) {
                throw new IllegalArgumentException("Unknown tax class: " + classCode);
            }
        }
        List<ComponentDto> components = r.components() == null ? List.of() : r.components();
        if (r.treatment() == TaxTreatment.TAXABLE && components.isEmpty()) {
            throw new IllegalArgumentException("A taxable rule needs at least one component with a rate.");
        }
        if (r.treatment() != TaxTreatment.TAXABLE && !components.isEmpty()) {
            throw new IllegalArgumentException("A " + r.treatment().name().toLowerCase(Locale.ROOT).replace('_', '-') + " rule has no components.");
        }
        Set<String> codes = new HashSet<>();
        for (ComponentDto c : components) {
            if (!codes.add(c.code().trim().toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Component code " + c.code().trim().toUpperCase(Locale.ROOT) + " is used twice.");
            }
        }
        rule.setName(r.name().trim());
        rule.setCountryCode(country);
        rule.setStateCode(state);
        rule.setJurisdiction(r.jurisdiction() == null || r.jurisdiction().isBlank() ? null : r.jurisdiction().trim());
        rule.setTaxType(r.taxType());
        rule.setProductClassCode(classCode);
        rule.setCustomerType(r.customerType() == null ? CustomerTypeCondition.ANY : r.customerType());
        rule.setSupplyType(r.supplyType() == null ? SupplyType.ANY : r.supplyType());
        rule.setTaxIdCondition(r.taxIdCondition() == null ? TaxIdCondition.ANY : r.taxIdCondition());
        rule.setTreatment(r.treatment());
        rule.setEffectiveFrom(r.effectiveFrom());
        rule.setEffectiveTo(r.effectiveTo());
        rule.setNotes(r.notes() == null || r.notes().isBlank() ? null : r.notes().trim());
        rule.setUpdatedAt(Instant.now());
        rule.getComponents().clear();
        int order = 0;
        for (ComponentDto c : components) {
            TaxRuleComponent cc = new TaxRuleComponent();
            cc.setRule(rule);
            cc.setSortOrder(order++);
            cc.setCode(c.code().trim().toUpperCase(Locale.ROOT));
            cc.setName(c.name().trim());
            cc.setRate(c.rate());
            cc.setCompound(c.compound());
            rule.getComponents().add(cc);
        }
    }

    private void validateComplete(TaxRule rule) {
        if (rule.getTreatment() == TaxTreatment.TAXABLE && rule.getComponents().isEmpty()) {
            throw new IllegalArgumentException("A taxable rule needs at least one component with a rate.");
        }
    }

    private void audit(String actorSub, String action, TaxRule rule, String detail) {
        auditService.recordSuccess(action, actorSub, null, null, "TaxRule", String.valueOf(rule.getId()), null, detail);
    }

    private static int latestVersionOf(List<TaxRule> all, String ruleKey) {
        return all.stream().filter(r -> r.getRuleKey().equals(ruleKey)).mapToInt(TaxRule::getVersion).max().orElse(1);
    }

    private TaxRuleDto toDto(TaxRule r, LocalDate today, int latestVersion) {
        List<ComponentDto> components = r.orderedComponents().stream().map(c -> new ComponentDto(c.getCode(), c.getName(), c.getRate(), c.isCompound())).toList();
        BigDecimal total = r.orderedComponents().stream().map(TaxRuleComponent::getRate).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TaxRuleDto(r.getId(), r.getRuleKey(), r.getVersion(), r.getVersion() == latestVersion, r.getName(), r.getCountryCode(),
            CountryResolver.nameOf(r.getCountryCode()), r.getStateCode(), StateCatalog.displayName(r.getCountryCode(), r.getStateCode()), r.getJurisdiction(),
            r.getTaxType(), r.getProductClassCode(), r.getCustomerType(), r.getSupplyType(), r.getTaxIdCondition(), r.getTreatment(), r.getEffectiveFrom(),
            r.getEffectiveTo(), r.getApprovalStatus(), r.isActive(), r.isInForce(today), r.getNotes(), r.getRejectionReason(), r.getCreatedAt(),
            actorLabels.label(r.getCreatedBy()), actorLabels.label(r.getLastEditedBy()), r.getApprovedAt(), actorLabels.label(r.getApprovedBy()), components, total);
    }
}
