package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRegistrationRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxRegistrationDto;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRegistration;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRegistrationRepository;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** REQ-BIL-002.37: the supplier's tax registrations kept in EIS. The business profile's GSTIN is listed too (read-only here). */
@Service
@RequiredArgsConstructor
public class TaxRegistrationService {

    private final TaxRegistrationRepository repository;
    private final BillingSettingsService billingSettingsService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TaxRegistrationDto> list() {
        List<TaxRegistrationDto> out = new ArrayList<>();
        BillingSettings p = billingSettingsService.current();
        if (p.getBusinessGstin() != null && !p.getBusinessGstin().isBlank()) {
            out.add(new TaxRegistrationDto(null, "IN", CountryResolver.nameOf("IN"), null, p.getBusinessState(), TaxType.GST, p.getBusinessGstin(),
                null, null, true, true));
        }
        repository.findAllByOrderByCountryCodeAscIdAsc().forEach(r -> out.add(toDto(r)));
        return out;
    }

    @Transactional
    public TaxRegistrationDto create(String actorSub, SaveTaxRegistrationRequest r) {
        TaxRegistration row = new TaxRegistration();
        apply(row, r);
        row = repository.save(row);
        auditService.recordSuccess("TAX_REGISTRATION_CREATED", actorSub, null, null, "TaxRegistration", String.valueOf(row.getId()), null,
            "Tax registration " + row.getTaxType() + " " + row.getCountryCode() + " added");
        return toDto(row);
    }

    @Transactional
    public TaxRegistrationDto update(String actorSub, Long id, SaveTaxRegistrationRequest r) {
        TaxRegistration row = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax registration not found"));
        apply(row, r);
        row = repository.save(row);
        auditService.recordSuccess("TAX_REGISTRATION_UPDATED", actorSub, null, null, "TaxRegistration", String.valueOf(row.getId()), null,
            "Tax registration " + row.getTaxType() + " " + row.getCountryCode() + " updated");
        return toDto(row);
    }

    @Transactional
    public void delete(String actorSub, Long id) {
        TaxRegistration row = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax registration not found"));
        repository.delete(row);
        auditService.recordSuccess("TAX_REGISTRATION_DELETED", actorSub, null, null, "TaxRegistration", String.valueOf(id), null,
            "Tax registration " + row.getTaxType() + " " + row.getCountryCode() + " deleted");
    }

    private void apply(TaxRegistration row, SaveTaxRegistrationRequest r) {
        String country = r.countryCode().trim().toUpperCase(Locale.ROOT);
        if (!CountryResolver.isKnownCode(country)) {
            throw new IllegalArgumentException("Unknown country code: " + country);
        }
        if (r.effectiveFrom() != null && r.effectiveTo() != null && r.effectiveTo().isBefore(r.effectiveFrom())) {
            throw new IllegalArgumentException("The end date cannot be before the start date.");
        }
        String state = null;
        if (r.stateCode() != null && !r.stateCode().isBlank()) {
            state = StateCatalog.normalize(country, r.stateCode()).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + r.stateCode()));
        }
        row.setCountryCode(country);
        row.setStateCode(state);
        row.setTaxType(r.taxType());
        row.setRegistrationNumber(r.registrationNumber().trim());
        row.setEffectiveFrom(r.effectiveFrom());
        row.setEffectiveTo(r.effectiveTo());
        row.setEnabled(r.enabled() == null || r.enabled());
        row.setUpdatedAt(Instant.now());
    }

    private TaxRegistrationDto toDto(TaxRegistration r) {
        return new TaxRegistrationDto(r.getId(), r.getCountryCode(), CountryResolver.nameOf(r.getCountryCode()), r.getStateCode(),
            StateCatalog.displayName(r.getCountryCode(), r.getStateCode()), r.getTaxType(), r.getRegistrationNumber(), r.getEffectiveFrom(),
            r.getEffectiveTo(), r.isEnabled(), false);
    }
}
