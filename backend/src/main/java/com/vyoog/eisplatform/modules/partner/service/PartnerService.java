package com.vyoog.eisplatform.modules.partner.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.partner.dto.ApplyAsProviderRequest;
import com.vyoog.eisplatform.modules.partner.dto.CreateOrUpdateContractRequest;
import com.vyoog.eisplatform.modules.partner.dto.PartnerContractDto;
import com.vyoog.eisplatform.modules.partner.dto.ProviderDto;
import com.vyoog.eisplatform.modules.partner.model.ContractStatus;
import com.vyoog.eisplatform.modules.partner.model.PartnerContract;
import com.vyoog.eisplatform.modules.partner.model.Provider;
import com.vyoog.eisplatform.modules.partner.model.ProviderStatus;
import com.vyoog.eisplatform.modules.partner.repository.PartnerContractRepository;
import com.vyoog.eisplatform.modules.partner.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 14.01 Provider Onboarding (sprint 2027.2.1, decision C42). Register is
 * public self-service; Verify, Approve and Activate are each their own
 * platform-admin decision (`MANAGE_PARTNERS`), made at their own time —
 * unlike Order/Review, there's no single moment here that legitimately
 * folds all of them into one action.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerService {

    private final ProviderRepository providerRepository;
    private final PartnerContractRepository contractRepository;
    private final AuditService auditService;

    /** 14.01.01.01 Register provider. */
    @Transactional
    public ProviderDto apply(ApplyAsProviderRequest request) {
        Provider provider = new Provider();
        provider.setName(request.name());
        provider.setContactName(request.contactName());
        provider.setContactEmail(request.contactEmail());
        provider.setDescription(request.description());
        provider.setStatus(ProviderStatus.REGISTERED);
        provider = providerRepository.save(provider);
        auditService.recordSuccess("PROVIDER_REGISTERED", null, null, request.contactEmail(),
            "Provider", provider.getId().toString(), null, "Provider application submitted: " + request.name());
        return toDto(provider);
    }

    public List<ProviderDto> listAll() {
        return providerRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProviderDto get(Long providerId) {
        return toDto(findProvider(providerId));
    }

    /** 14.01.01.02 Verify provider. */
    @Transactional
    public ProviderDto verify(Long providerId) {
        return transition(providerId, ProviderStatus.REGISTERED, ProviderStatus.VERIFIED, "PROVIDER_VERIFIED");
    }

    /** 14.01.01.03 Approve provider. */
    @Transactional
    public ProviderDto approve(Long providerId) {
        return transition(providerId, ProviderStatus.VERIFIED, ProviderStatus.APPROVED, "PROVIDER_APPROVED");
    }

    /** 14.01.01.04 Activate provider. */
    @Transactional
    public ProviderDto activate(Long providerId) {
        return transition(providerId, ProviderStatus.APPROVED, ProviderStatus.ACTIVE, "PROVIDER_ACTIVATED");
    }

    /** Refuses a provider already ACTIVE or REJECTED — every other stage
     * (REGISTERED, VERIFIED, APPROVED) may still be rejected. */
    @Transactional
    public ProviderDto reject(Long providerId) {
        Provider provider = findProvider(providerId);
        if (provider.getStatus() == ProviderStatus.ACTIVE || provider.getStatus() == ProviderStatus.REJECTED) {
            throw new IllegalArgumentException("This provider can no longer be rejected.");
        }
        provider.setStatus(ProviderStatus.REJECTED);
        provider = providerRepository.save(provider);
        auditService.recordSuccess("PROVIDER_REJECTED", null, null, null,
            "Provider", providerId.toString(), null, "Provider application rejected");
        return toDto(provider);
    }

    private ProviderDto transition(Long providerId, ProviderStatus expected, ProviderStatus next, String auditAction) {
        Provider provider = findProvider(providerId);
        if (provider.getStatus() != expected) {
            throw new IllegalArgumentException(
                "This provider must be " + expected + " before it can move to " + next + ".");
        }
        provider.setStatus(next);
        provider = providerRepository.save(provider);
        auditService.recordSuccess(auditAction, null, null, null,
            "Provider", providerId.toString(), null, "Provider moved to " + next);
        return toDto(provider);
    }

    /** 14.01.02.01/.02 Create contract / Manage terms — the same upsert. */
    @Transactional
    public PartnerContractDto createOrUpdateContract(Long providerId, CreateOrUpdateContractRequest request) {
        findProvider(providerId);
        if (!request.endDate().isAfter(request.startDate())) {
            throw new IllegalArgumentException("A contract's end date must be after its start date.");
        }
        PartnerContract contract = contractRepository.findByProviderId(providerId)
            .orElseGet(() -> {
                PartnerContract created = new PartnerContract();
                created.setProviderId(providerId);
                return created;
            });
        contract.setTerms(request.terms());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        // Editing a contract's terms reactivates it — same reasoning as a
        // review edit resetting to PENDING (C41): a renegotiated contract
        // shouldn't stay flagged EXPIRED once its dates have been extended.
        contract.setStatus(ContractStatus.ACTIVE);
        contract = contractRepository.save(contract);
        auditService.recordSuccess("PARTNER_CONTRACT_SAVED", null, null, null,
            "PartnerContract", contract.getId().toString(), null, "Contract saved for provider " + providerId);
        return toContractDto(contract);
    }

    public PartnerContractDto getContract(Long providerId) {
        return contractRepository.findByProviderId(providerId)
            .map(this::toContractDto)
            .orElseThrow(() -> new ResourceNotFoundException("No contract on file for this provider"));
    }

    /** 14.01.02.03 Track expiration — flips ACTIVE contracts whose end date
     * has passed. Called by the scheduled {@code ContractExpiryJob}. */
    @Transactional
    public int expireOverdueContracts() {
        List<PartnerContract> overdue = contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now());
        for (PartnerContract contract : overdue) {
            contract.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(contract);
            auditService.recordSuccess("PARTNER_CONTRACT_EXPIRED", null, null, null,
                "PartnerContract", contract.getId().toString(), null, "Contract expired automatically");
        }
        return overdue.size();
    }

    private Provider findProvider(Long providerId) {
        return providerRepository.findById(providerId)
            .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
    }

    private ProviderDto toDto(Provider provider) {
        return new ProviderDto(provider.getId(), provider.getName(), provider.getContactName(),
            provider.getContactEmail(), provider.getDescription(), provider.getStatus(), provider.getCreatedAt());
    }

    private PartnerContractDto toContractDto(PartnerContract contract) {
        return new PartnerContractDto(contract.getId(), contract.getProviderId(), contract.getTerms(),
            contract.getStartDate(), contract.getEndDate(), contract.getStatus());
    }
}
