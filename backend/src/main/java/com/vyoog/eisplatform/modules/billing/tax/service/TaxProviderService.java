package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.billing.tax.dto.ValidationReportDto;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRegistration;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxSettings;
import com.vyoog.eisplatform.modules.billing.tax.model.ValidationStatus;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderCapabilities;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderContext;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderException;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProviderRegistry;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRegistrationRepository;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** REQ-BIL-002.4, .33: the provider checks that must pass before external mode is switched on, and the stored result.
 * Calls the provider, never stores a credential. */
@Service
@RequiredArgsConstructor
public class TaxProviderService {

    private final TaxProviderRegistry registry;
    private final BillingSettingsService billingSettingsService;
    private final TaxRegistrationRepository registrationRepository;
    private final ProductPlanRepository productPlanRepository;

    public Optional<TaxProvider> providerOf(TaxSettings settings) {
        return registry.find(settings.getProviderCode());
    }

    public ProviderContext context(TaxSettings settings) {
        return new ProviderContext(settings.getProviderCompanyCode(), settings.getProviderEnvironment(), Duration.ofSeconds(settings.getProviderTimeoutSeconds()));
    }

    /** Runs every check, stores the result on {@code settings} (the caller saves it) and returns it for the screen. Every check is
     * reported, so the administrator sees all that need fixing at once. */
    public ValidationReportDto validate(TaxSettings settings) {
        List<ValidationReportDto.Check> checks = new ArrayList<>();
        Optional<TaxProvider> provider = providerOf(settings);
        checks.add(check("PROVIDER_SELECTED", provider.isPresent(), "A tax provider is selected",
            provider.isPresent() ? provider.get().displayName() : "Select a provider and save its settings."));

        ProviderCapabilities capabilities = null;
        if (provider.isPresent()) {
            boolean credentials = provider.get().credentialsPresent();
            checks.add(check("CREDENTIALS", credentials, "Credentials are present on the backend",
                credentials ? "All required values are set." : "Set " + provider.get().credentialStatus().entrySet().stream()
                    .filter(e -> !e.getValue() && !e.getKey().endsWith("_SECRET")).map(java.util.Map.Entry::getKey)
                    .collect(Collectors.joining(", ")) + " in the backend environment (config/secrets.env)."));
            if (credentials) {
                try {
                    capabilities = provider.get().testConnection(context(settings));
                    checks.add(check("CONNECTION", true, "The provider answers and accepts the credentials",
                        "Provider: " + capabilities.providerName() + "."));
                } catch (ProviderException e) {
                    checks.add(check("CONNECTION", false, "The provider answers and accepts the credentials", e.getMessage()));
                }
            }
        }

        BillingSettings profile = billingSettingsService.current();
        Optional<String> supplierCountry = CountryResolver.resolve(profile.getBusinessCountry());
        boolean legalEntity = profile.getBusinessLegalName() != null && !profile.getBusinessLegalName().isBlank() && supplierCountry.isPresent();
        checks.add(check("SUPPLIER_ENTITY", legalEntity, "The supplier's legal entity is set",
            legalEntity ? profile.getBusinessLegalName() + ", " + CountryResolver.nameOf(supplierCountry.get()) + "."
                : "Fill in the legal name and country in Billing settings, Business and invoicing."));
        boolean registration = profile.getBusinessGstin() != null && !profile.getBusinessGstin().isBlank()
            || registrationRepository.findAll().stream().anyMatch(r -> r.isInForce(LocalDate.now(ZoneOffset.UTC)));
        checks.add(check("SUPPLIER_REGISTRATION", registration, "The supplier has a tax registration",
            registration ? "A registration is on file." : "Add a registration under Tax Configuration, Registrations, or the GSTIN in Billing settings."));

        if (capabilities != null) {
            if (supplierCountry.isPresent()) {
                checks.add(check("COUNTRY_SUPPORT", capabilities.supportsCountry(supplierCountry.get()), "The provider supports the supplier's country",
                    supplierCountry.get() + (capabilities.supportsCountry(supplierCountry.get()) ? " is supported." : " is not in the provider's list.")));
            }
            Set<String> used = productPlanRepository.findAll().stream().map(ProductPlan::getCurrency).filter(java.util.Objects::nonNull)
                .map(Currency::name).collect(Collectors.toCollection(TreeSet::new));
            Set<String> missing = new TreeSet<>(used);
            missing.removeAll(capabilities.currencies());
            checks.add(check("CURRENCY_SUPPORT", missing.isEmpty(), "The provider supports the currencies of the plans",
                missing.isEmpty() ? (used.isEmpty() ? "No plan uses a currency yet." : String.join(", ", used) + " supported.")
                    : "Not supported by the provider: " + String.join(", ", missing) + "."));
            checks.add(check("TRANSACTION_TYPE", capabilities.supportsTransactionType("SALE"), "The provider supports sales transactions",
                capabilities.supportsTransactionType("SALE") ? "SALE is supported." : "SALE is not in the provider's list."));
        }

        boolean passed = checks.stream().allMatch(ValidationReportDto.Check::passed);
        Instant now = Instant.now();
        settings.setValidationStatus(passed ? ValidationStatus.PASSED : ValidationStatus.FAILED);
        settings.setValidatedAt(now);
        String summary = passed ? "All checks passed." : checks.stream().filter(c -> !c.passed()).map(c -> c.label() + ": " + c.detail())
            .collect(Collectors.joining(" "));
        settings.setValidationSummary(summary.length() > 2000 ? summary.substring(0, 2000) : summary);
        if (capabilities != null) {
            settings.setProviderCountries(String.join(",", capabilities.countries()));
            settings.setProviderCurrencies(String.join(",", capabilities.currencies()));
            settings.setProviderTransactionTypes(String.join(",", capabilities.transactionTypes()));
        } else {
            settings.setProviderCountries(null);
            settings.setProviderCurrencies(null);
            settings.setProviderTransactionTypes(null);
        }
        return new ValidationReportDto(passed, now, checks,
            capabilities == null ? List.of() : List.copyOf(new TreeSet<>(capabilities.countries())),
            capabilities == null ? List.of() : List.copyOf(new TreeSet<>(capabilities.currencies())),
            capabilities == null ? List.of() : List.copyOf(new TreeSet<>(capabilities.transactionTypes())));
    }

    /** The supplier's registrations as sent to a provider: the business profile's GSTIN plus the enabled rows in force. */
    public List<TaxRegistration> registrationsInForce(LocalDate date) {
        return registrationRepository.findAllByOrderByCountryCodeAscIdAsc().stream().filter(r -> r.isInForce(date)).toList();
    }

    private static ValidationReportDto.Check check(String code, boolean passed, String label, String detail) {
        return new ValidationReportDto.Check(code, passed, label, detail);
    }
}
