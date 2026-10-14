package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxExemptionRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxExemptionDto;
import com.vyoog.eisplatform.modules.billing.tax.model.ExemptionStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxExemption;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxExemptionRepository;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/** REQ-BIL-002.18 / BR-19: exemption certificates for a customer or an organization. Created pending, approved by a different
 * administrator (BR-22), and then used by the internal engine to return an explicit Exempt result. */
@Service
@RequiredArgsConstructor
public class TaxExemptionService {

    private final TaxExemptionRepository repository;
    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final TaxSettingsService settingsService;
    private final TaxActorLabels actorLabels;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TaxExemptionDto> list() {
        return repository.findAllByOrderByIdDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public TaxExemptionDto create(String actorSub, SaveTaxExemptionRequest r) {
        String ownerType = r.ownerType().trim().toUpperCase(Locale.ROOT);
        TaxExemption e = new TaxExemption();
        if ("CUSTOMER".equals(ownerType)) {
            customerRepository.findById(r.ownerId()).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            e.setOwnerCustomerId(r.ownerId());
        } else if ("ORGANIZATION".equals(ownerType)) {
            organizationRepository.findById(r.ownerId()).orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
            e.setOwnerOrganizationId(r.ownerId());
        } else {
            throw new IllegalArgumentException("Owner type must be CUSTOMER or ORGANIZATION.");
        }
        String country = r.countryCode().trim().toUpperCase(Locale.ROOT);
        if (!CountryResolver.isKnownCode(country)) {
            throw new IllegalArgumentException("Unknown country code: " + country);
        }
        if (r.effectiveTo() != null && r.effectiveTo().isBefore(r.effectiveFrom())) {
            throw new IllegalArgumentException("The end date cannot be before the start date.");
        }
        e.setCountryCode(country);
        e.setReason(r.reason().trim());
        e.setCertificateNumber(r.certificateNumber() == null || r.certificateNumber().isBlank() ? null : r.certificateNumber().trim());
        e.setEffectiveFrom(r.effectiveFrom());
        e.setEffectiveTo(r.effectiveTo());
        e.setStatus(ExemptionStatus.PENDING_APPROVAL);
        e.setCreatedBy(actorSub);
        e = repository.save(e);
        audit(actorSub, "TAX_EXEMPTION_CREATED", e, "Tax exemption created for " + ownerType.toLowerCase(Locale.ROOT) + " " + r.ownerId() + " in " + country + " (pending approval)");
        return toDto(e);
    }

    @Transactional
    public TaxExemptionDto approve(String actorSub, Long id) {
        TaxExemption e = find(id);
        if (e.getStatus() != ExemptionStatus.PENDING_APPROVAL) {
            throw new BillingConflictException("Only an exemption that is pending approval can be approved.");
        }
        if (!settingsService.current().isAllowSelfApproval() && actorSub != null && actorSub.equals(e.getCreatedBy())) {
            throw new BillingConflictException("A different administrator must approve this exemption: you created it.");
        }
        e.setStatus(ExemptionStatus.APPROVED);
        e.setApprovedBy(actorSub);
        e.setApprovedAt(Instant.now());
        repository.save(e);
        audit(actorSub, "TAX_EXEMPTION_APPROVED", e, "Tax exemption " + id + " approved");
        return toDto(e);
    }

    @Transactional
    public TaxExemptionDto revoke(String actorSub, Long id) {
        TaxExemption e = find(id);
        if (e.getStatus() == ExemptionStatus.REVOKED) {
            return toDto(e);
        }
        e.setStatus(ExemptionStatus.REVOKED);
        repository.save(e);
        audit(actorSub, "TAX_EXEMPTION_REVOKED", e, "Tax exemption " + id + " revoked");
        return toDto(e);
    }

    private TaxExemption find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax exemption not found"));
    }

    private void audit(String actorSub, String action, TaxExemption e, String detail) {
        auditService.recordSuccess(action, actorSub, null, null, "TaxExemption", String.valueOf(e.getId()), e.getOwnerOrganizationId(), detail);
    }

    private TaxExemptionDto toDto(TaxExemption e) {
        boolean org = e.getOwnerOrganizationId() != null;
        Long ownerId = org ? e.getOwnerOrganizationId() : e.getOwnerCustomerId();
        String label = org ? organizationRepository.findById(ownerId).map(o -> o.getName()).orElse("Organization #" + ownerId)
            : customerRepository.findById(ownerId).map(c -> c.getFirstName() + " " + c.getLastName() + " (" + c.getEmail() + ")").orElse("Customer #" + ownerId);
        return new TaxExemptionDto(e.getId(), org ? "ORGANIZATION" : "CUSTOMER", ownerId, label, e.getCountryCode(), CountryResolver.nameOf(e.getCountryCode()),
            e.getReason(), e.getCertificateNumber(), e.getEffectiveFrom(), e.getEffectiveTo(), e.getStatus(), e.isInForce(LocalDate.now(ZoneOffset.UTC)),
            actorLabels.label(e.getCreatedBy()), e.getCreatedAt(), actorLabels.label(e.getApprovedBy()), e.getApprovedAt());
    }
}
