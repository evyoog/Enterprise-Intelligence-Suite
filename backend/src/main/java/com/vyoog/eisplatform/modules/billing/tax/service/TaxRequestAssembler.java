package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRegistration;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import com.vyoog.eisplatform.modules.billing.tax.support.TaxIdValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** REQ-BIL-002.31 steps 1 to 3: gathers the supplier's legal entity and registrations (Billing settings and the registrations
 * table), the customer's billing details and the lines into one engine-neutral request. */
@Service
@RequiredArgsConstructor
public class TaxRequestAssembler {

    private final BillingSettingsService billingSettingsService;
    private final BillingDetailsRepository billingDetailsRepository;
    private final TaxProviderService providerService;

    public TaxCalculationRequest assemble(String transactionRef, TransactionType type, LocalDate date, String currency, Long ownerCustomerId,
                                          Long ownerOrganizationId, List<TaxCalculationRequest.Line> lines) {
        return new TaxCalculationRequest(transactionRef, type, date, currency, supplier(date), customer(ownerCustomerId, ownerOrganizationId), lines);
    }

    public TaxCalculationRequest.Supplier supplier(LocalDate date) {
        BillingSettings p = billingSettingsService.current();
        String country = CountryResolver.resolve(p.getBusinessCountry()).orElse(null);
        String stateKey = country == null ? null : StateCatalog.normalize(country, p.getBusinessState()).orElse(null);
        if (stateKey == null && "IN".equals(country) && TaxIdValidator.isValidGstin(p.getBusinessGstin())) {
            stateKey = TaxIdValidator.gstinState(p.getBusinessGstin()).map(StateCatalog.State::code).orElse(null);
        }
        List<TaxCalculationRequest.Registration> registrations = providerService.registrationsInForce(date).stream()
            .map(this::registration).toList();
        return new TaxCalculationRequest.Supplier(p.getBusinessLegalName(), p.getBusinessAddressLine1(), p.getBusinessCity(), p.getBusinessState(),
            stateKey, p.getBusinessPostalCode(), country, p.getBusinessGstin(), registrations);
    }

    public TaxCalculationRequest.Customer customer(Long ownerCustomerId, Long ownerOrganizationId) {
        CustomerTypeCondition kind = ownerOrganizationId != null ? CustomerTypeCondition.BUSINESS : CustomerTypeCondition.INDIVIDUAL;
        Optional<BillingDetails> details = ownerCustomerId != null ? billingDetailsRepository.findByOwnerCustomerId(ownerCustomerId)
            : billingDetailsRepository.findByOwnerOrganizationId(ownerOrganizationId);
        if (details.isEmpty()) {
            return new TaxCalculationRequest.Customer(kind, ownerCustomerId, ownerOrganizationId, null, null, null, null, null, null, null, null, null);
        }
        BillingDetails d = details.get();
        String country = CountryResolver.resolve(d.getCountry()).orElse(null);
        String stateKey = country == null ? null : StateCatalog.normalize(country, d.getState()).orElse(null);
        return new TaxCalculationRequest.Customer(kind, ownerCustomerId, ownerOrganizationId, d.getBillingName(), d.getAddressLine1(), d.getCity(),
            d.getState(), stateKey, d.getPostalCode(), d.getCountry(), country, d.getTaxId());
    }

    private TaxCalculationRequest.Registration registration(TaxRegistration r) {
        String stateKey = r.getStateCode() == null ? null : StateCatalog.normalize(r.getCountryCode(), r.getStateCode()).orElse(r.getStateCode());
        return new TaxCalculationRequest.Registration(r.getCountryCode(), stateKey, r.getTaxType(), r.getRegistrationNumber());
    }
}
