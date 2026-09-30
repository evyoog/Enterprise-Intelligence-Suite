package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.BillingDetailsDto;
import com.vyoog.eisplatform.modules.billing.dto.SaveBillingDetailsRequest;
import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** REQ-BIL-001.1. */
@Service
@RequiredArgsConstructor
public class BillingDetailsService {

    private final BillingDetailsRepository billingDetailsRepository;
    private final AuditService auditService;

    /** 404, not a null 200 body — a controller method that returns null
     * makes Spring write an empty response with no JSON at all (not even
     * the literal {@code null}), which breaks a JSON client expecting a
     * parseable body. Same "not saved yet" convention as
     * {@code ReviewsController#getMine} elsewhere in this codebase: the
     * frontend catches the 404 and treats it as "nothing saved yet". */
    public BillingDetailsDto getForCustomer(Long customerId) {
        return billingDetailsRepository.findByOwnerCustomerId(customerId).map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("No billing details saved yet"));
    }

    public BillingDetailsDto getForOrganization(Long organizationId) {
        return billingDetailsRepository.findByOwnerOrganizationId(organizationId).map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("No billing details saved yet"));
    }

    @Transactional
    public BillingDetailsDto saveForCustomer(Long customerId, SaveBillingDetailsRequest request) {
        BillingDetails details = billingDetailsRepository.findByOwnerCustomerId(customerId).orElseGet(() -> {
            BillingDetails created = new BillingDetails();
            created.setOwnerCustomerId(customerId);
            return created;
        });
        apply(details, request);
        details = billingDetailsRepository.save(details);
        auditService.recordSuccess("BILLING_DETAILS_SAVED", null, customerId, null,
            "BillingDetails", details.getId().toString(), null, "Billing details saved");
        return toDto(details);
    }

    @Transactional
    public BillingDetailsDto saveForOrganization(Long customerId, Long organizationId, SaveBillingDetailsRequest request) {
        BillingDetails details = billingDetailsRepository.findByOwnerOrganizationId(organizationId).orElseGet(() -> {
            BillingDetails created = new BillingDetails();
            created.setOwnerOrganizationId(organizationId);
            return created;
        });
        apply(details, request);
        details = billingDetailsRepository.save(details);
        auditService.recordSuccess("BILLING_DETAILS_SAVED", null, customerId, null,
            "BillingDetails", details.getId().toString(), organizationId, "Billing details saved");
        return toDto(details);
    }

    private void apply(BillingDetails details, SaveBillingDetailsRequest request) {
        details.setBillingName(request.billingName());
        details.setBillingEmail(request.billingEmail());
        details.setAddressLine1(request.addressLine1());
        details.setAddressLine2(request.addressLine2());
        details.setCity(request.city());
        details.setState(request.state());
        details.setPostalCode(request.postalCode());
        details.setCountry(request.country());
        details.setTaxId(request.taxId());
    }

    /** Free-text snapshot copied onto an invoice at issue time (Invoice's
     * own javadoc) — null-safe so an invoice can still be generated even if
     * billing details were never filled in. */
    public String snapshotFor(BillingDetails details) {
        if (details == null) {
            return "Not specified";
        }
        return details.getBillingName() + "\n" + details.getAddressLine1()
            + (details.getAddressLine2() != null ? "\n" + details.getAddressLine2() : "")
            + "\n" + details.getCity() + ", " + details.getState() + " " + details.getPostalCode()
            + "\n" + details.getCountry();
    }

    private BillingDetailsDto toDto(BillingDetails details) {
        return new BillingDetailsDto(details.getId(), details.getBillingName(), details.getBillingEmail(),
            details.getAddressLine1(), details.getAddressLine2(), details.getCity(), details.getState(),
            details.getPostalCode(), details.getCountry(), details.getTaxId(), details.getUpdatedAt());
    }
}
