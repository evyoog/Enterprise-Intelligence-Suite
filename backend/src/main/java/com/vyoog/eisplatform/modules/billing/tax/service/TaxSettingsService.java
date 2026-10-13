package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveApprovalPolicyRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveProviderSettingsRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxModeResultDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxStatusDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.ValidationReportDto;
import com.vyoog.eisplatform.modules.billing.tax.model.ProviderEnvironment;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxSettings;
import com.vyoog.eisplatform.modules.billing.tax.model.ValidationStatus;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProviderRegistry;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxCalculationRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** REQ-BIL-002.1 to .6 and .40/.41: the External Tax Engine switch, the provider settings, the status shown on Tax
 * Configuration and the pinned snapshot a calculation reads once at its start (BR-2). The setting lives in the database, so it
 * survives sign-out, restart and deployment (REQ-BIL-002.3). */
@Service
@RequiredArgsConstructor
public class TaxSettingsService {

    private final TaxSettingsRepository settingsRepository;
    private final TaxRuleRepository ruleRepository;
    private final TaxCalculationRepository calculationRepository;
    private final InvoiceRepository invoiceRepository;
    private final TaxProviderRegistry providerRegistry;
    private final TaxProviderService providerService;
    private final TaxActorLabels actorLabels;
    private final AuditService auditService;

    /** The one settings row; created with the defaults (External Tax Engine off) on first use. */
    @Transactional
    public TaxSettings current() {
        return settingsRepository.findById(TaxSettings.SINGLETON_ID).orElseGet(() -> settingsRepository.save(new TaxSettings()));
    }

    /** BR-2: read once at the start of a calculation. */
    @Transactional
    public TaxModeSnapshot snapshot() {
        TaxSettings s = current();
        return new TaxModeSnapshot(s.isExternalEngineEnabled() ? TaxEngineType.EXTERNAL : TaxEngineType.INTERNAL, s.getConfigVersion(),
            s.getProviderCode(), s.getProviderCompanyCode(), s.getProviderEnvironment(), s.getProviderTimeoutSeconds(), s.getProviderMaxAttempts(),
            s.getValidationStatus(), csv(s.getProviderCountries()), csv(s.getProviderCurrencies()), csv(s.getProviderTransactionTypes()));
    }

    /** REQ-BIL-002.4/.5/.6. Switching on runs the provider checks in this request; a failure leaves the setting unchanged. */
    @Transactional
    public TaxModeResultDto setExternalEngineEnabled(String actorSub, boolean enable) {
        TaxSettings s = current();
        if (enable == s.isExternalEngineEnabled()) {
            return new TaxModeResultDto(false, status(), null);
        }
        ValidationReportDto report = null;
        if (enable) {
            report = providerService.validate(s);
            if (!report.passed()) {
                settingsRepository.save(s);
                auditService.recordFailure("TAX_ENGINE_SWITCH_REFUSED", null, "External Tax Engine could not be switched on: " + s.getValidationSummary());
                return new TaxModeResultDto(false, status(), report);
            }
        }
        boolean was = s.isExternalEngineEnabled();
        s.setExternalEngineEnabled(enable);
        s.setConfigVersion(s.getConfigVersion() + 1);
        s.setModeChangedAt(Instant.now());
        s.setModeChangedBy(actorSub);
        s.setUpdatedAt(Instant.now());
        s.setUpdatedBy(actorSub);
        settingsRepository.save(s);
        auditService.recordSuccess("TAX_ENGINE_MODE_CHANGED", actorSub, null, null, "TaxSettings", String.valueOf(s.getId()), null,
            "Tax calculation mode changed from " + label(was) + " to " + label(enable) + " (configuration version " + s.getConfigVersion() + ")");
        return new TaxModeResultDto(true, status(), report);
    }

    /** Provider code, company code, environment, timeout and attempts. While external mode is on, the new settings must pass
     * validation too (BR-4); otherwise the change is refused and nothing is saved. */
    @Transactional
    public TaxStatusDto saveProviderSettings(String actorSub, SaveProviderSettingsRequest r) {
        TaxProvider provider = providerRegistry.find(r.providerCode().trim())
            .orElseThrow(() -> new IllegalArgumentException("Unknown tax provider: " + r.providerCode()));
        ProviderEnvironment environment;
        try {
            environment = ProviderEnvironment.valueOf(r.environment().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Environment must be SANDBOX or PRODUCTION.");
        }
        TaxSettings s = current();
        s.setProviderCode(provider.code());
        s.setProviderCompanyCode(r.companyCode() == null || r.companyCode().isBlank() ? null : r.companyCode().trim());
        s.setProviderEnvironment(environment);
        s.setProviderTimeoutSeconds(r.timeoutSeconds());
        s.setProviderMaxAttempts(r.maxAttempts());
        s.setConfigVersion(s.getConfigVersion() + 1);
        s.setUpdatedAt(Instant.now());
        s.setUpdatedBy(actorSub);
        if (s.isExternalEngineEnabled()) {
            ValidationReportDto report = providerService.validate(s);
            if (!report.passed()) {
                throw new BillingConflictException("The new provider settings did not pass validation, so nothing was saved: " + s.getValidationSummary());
            }
        } else {
            resetValidation(s);
        }
        settingsRepository.save(s);
        auditService.recordSuccess("TAX_PROVIDER_SETTINGS_CHANGED", actorSub, null, null, "TaxSettings", String.valueOf(s.getId()), null,
            "Tax provider settings saved: " + provider.code() + ", " + environment + ", company " + (s.getProviderCompanyCode() == null ? "-" : s.getProviderCompanyCode()));
        return status();
    }

    /** REQ-BIL-002.33 Test connection / Validate capabilities. Stores the result; never changes the switch. If external mode is on
     * and the checks now fail, the status shows it and calculations block (no silent fall-back). */
    @Transactional
    public ValidationReportDto validateProvider(String actorSub) {
        TaxSettings s = current();
        ValidationReportDto report = providerService.validate(s);
        s.setUpdatedAt(Instant.now());
        s.setUpdatedBy(actorSub);
        settingsRepository.save(s);
        auditService.recordSuccess("TAX_PROVIDER_VALIDATED", actorSub, null, null, "TaxSettings", String.valueOf(s.getId()), null,
            "Tax provider validation " + (report.passed() ? "passed" : "failed") + (report.passed() ? "" : ": " + s.getValidationSummary()));
        return report;
    }

    @Transactional
    public TaxStatusDto saveApprovalPolicy(String actorSub, SaveApprovalPolicyRequest r) {
        TaxSettings s = current();
        boolean was = s.isAllowSelfApproval();
        s.setAllowSelfApproval(r.allowSelfApproval());
        s.setUpdatedAt(Instant.now());
        s.setUpdatedBy(actorSub);
        settingsRepository.save(s);
        if (was != s.isAllowSelfApproval()) {
            auditService.recordSuccess("TAX_APPROVAL_POLICY_CHANGED", actorSub, null, null, "TaxSettings", String.valueOf(s.getId()), null,
                "Same-administrator approval of tax rules " + (s.isAllowSelfApproval() ? "allowed" : "not allowed"));
        }
        return status();
    }

    /** REQ-BIL-002.2: the active mode, its status and the numbers behind it. */
    @Transactional
    public TaxStatusDto status() {
        TaxSettings s = current();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long inForce = ruleRepository.countInForce(RuleApprovalStatus.APPROVED, today);
        long pending = ruleRepository.countByApprovalStatus(RuleApprovalStatus.PENDING_APPROVAL);
        long drafts = ruleRepository.countByApprovalStatus(RuleApprovalStatus.DRAFT);
        List<String> covered = ruleRepository.findAll().stream().filter(r -> r.isInForce(today)).map(TaxRule::getCountryCode)
            .distinct().sorted().toList();
        long waiting = invoiceRepository.countByStatus(InvoiceStatus.DRAFT);

        TaxProvider provider = providerService.providerOf(s).orElse(null);
        boolean configured = provider != null && provider.credentialsPresent();
        boolean external = s.isExternalEngineEnabled();

        List<TaxStatusDto.Message> messages = new ArrayList<>();
        String status = "READY";
        if (!external) {
            if (inForce == 0) {
                status = "BLOCKING";
                messages.add(new TaxStatusDto.Message("ERROR", "NO_RULE_IN_FORCE",
                    "No approved tax rule is in force. New invoices will wait as Draft until a rule is approved."));
            }
            if (pending > 0) {
                messages.add(new TaxStatusDto.Message("INFO", "RULES_PENDING", pending + " rule(s) wait for approval."));
            }
        } else {
            if (provider == null || !configured) {
                status = "BLOCKING";
                messages.add(new TaxStatusDto.Message("ERROR", "PROVIDER_NOT_CONFIGURED",
                    "The external provider is not configured on the backend. New invoices will wait as Draft."));
            } else if (s.getValidationStatus() != ValidationStatus.PASSED) {
                status = "BLOCKING";
                messages.add(new TaxStatusDto.Message("ERROR", "PROVIDER_NOT_VALIDATED",
                    "The provider has not passed validation. Run Test connection. New invoices will wait as Draft."));
            }
        }
        if (waiting > 0) {
            if (status.equals("READY")) {
                status = "ATTENTION";
            }
            messages.add(new TaxStatusDto.Message("WARNING", "INVOICES_WAITING", waiting + " invoice(s) are Draft because tax could not be determined."));
        }
        if (messages.isEmpty()) {
            messages.add(new TaxStatusDto.Message("INFO", "OK", external ? "The external provider is ready." : inForce + " approved rule(s) in force."));
        }

        TaxStatusDto.External ext = new TaxStatusDto.External(
            s.getProviderCode(), provider == null ? null : provider.displayName(),
            providerRegistry.all().stream().map(p -> new TaxStatusDto.ProviderOption(p.code(), p.displayName())).toList(),
            configured, provider == null ? java.util.Map.of() : provider.credentialStatus(), provider == null ? null : provider.maskedEndpoint(),
            s.getProviderEnvironment().name(), s.getProviderCompanyCode(), s.getProviderTimeoutSeconds(), s.getProviderMaxAttempts(),
            s.getValidationStatus().name(), s.getValidatedAt(), s.getValidationSummary(),
            sorted(s.getProviderCountries()), sorted(s.getProviderCurrencies()), sorted(s.getProviderTransactionTypes()));
        TaxStatusDto.LastCalculation last = calculationRepository.findFirstByOrderByIdDesc()
            .map(c -> new TaxStatusDto.LastCalculation(c.getCalculatedAt(), c.getEngine().name(), c.getStatus().name(), c.getReasonCode(), c.getReasonMessage()))
            .orElse(null);
        return new TaxStatusDto(external ? "EXTERNAL" : "INTERNAL", external ? "External tax provider" : "EIS internal tax engine", external,
            status, messages, s.getConfigVersion(), s.getModeChangedAt(), actorLabels.label(s.getModeChangedBy()), s.isAllowSelfApproval(),
            new TaxStatusDto.Internal(inForce, pending, drafts, covered), ext, waiting, last);
    }

    private static void resetValidation(TaxSettings s) {
        s.setValidationStatus(ValidationStatus.NOT_VALIDATED);
        s.setValidatedAt(null);
        s.setValidationSummary(null);
        s.setProviderCountries(null);
        s.setProviderCurrencies(null);
        s.setProviderTransactionTypes(null);
    }

    private static String label(boolean external) {
        return external ? "External tax provider" : "EIS internal tax engine";
    }

    private static Set<String> csv(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<String> sorted(String value) {
        return List.copyOf(new TreeSet<>(csv(value)));
    }
}
