package com.vyoog.eisplatform.modules.billing.tax.controller;

import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.BlockedInvoiceDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.ProductTaxClassDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.RejectRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveApprovalPolicyRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveProviderSettingsRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxClassRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxExemptionRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRegistrationRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRuleRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxCalculationDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxClassDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxExemptionDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxModeRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxModeResultDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxReferenceDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxRegistrationDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxRuleDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxStatusDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.ValidationReportDto;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxCalculationService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxClassService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxExemptionService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxReferenceService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxRegistrationService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxRuleService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REQ-BIL-002 — Platform Admin, Billing, Tax Configuration. {@code MANAGE_BILLING} is enforced in SecurityConfig for every path under
 * {@code /admin/billing/**}. The acting administrator is the Keycloak subject of the token (a platform administrator may have no customer row).
 * No endpoint accepts or returns a provider credential (BR-30). */
@RestController
@RequestMapping("/admin/billing/tax")
@RequiredArgsConstructor
public class AdminTaxController {

    private final TaxSettingsService settingsService;
    private final TaxRuleService ruleService;
    private final TaxClassService classService;
    private final TaxRegistrationService registrationService;
    private final TaxExemptionService exemptionService;
    private final TaxReferenceService referenceService;
    private final TaxCalculationService calculationService;
    private final InvoiceService invoiceService;

    // ---- engine switch, provider, status ----

    @GetMapping("/status")
    public TaxStatusDto status() {
        return settingsService.status();
    }

    /** 200 when the mode was set (or already was); 409 with the same body when switching on was refused, so the screen can list the failed checks. */
    @PutMapping("/mode")
    public ResponseEntity<TaxModeResultDto> mode(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TaxModeRequest request) {
        TaxModeResultDto result = settingsService.setExternalEngineEnabled(jwt.getSubject(), request.externalEngineEnabled());
        boolean refused = !result.changed() && result.report() != null;
        return ResponseEntity.status(refused ? HttpStatus.CONFLICT : HttpStatus.OK).body(result);
    }

    @PutMapping("/provider")
    public TaxStatusDto saveProvider(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveProviderSettingsRequest request) {
        return settingsService.saveProviderSettings(jwt.getSubject(), request);
    }

    /** Test connection and Validate capabilities (REQ-BIL-002.33). */
    @PostMapping("/provider/validate")
    public ValidationReportDto validateProvider(@AuthenticationPrincipal Jwt jwt) {
        return settingsService.validateProvider(jwt.getSubject());
    }

    @PutMapping("/approval-policy")
    public TaxStatusDto approvalPolicy(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveApprovalPolicyRequest request) {
        return settingsService.saveApprovalPolicy(jwt.getSubject(), request);
    }

    @GetMapping("/reference")
    public TaxReferenceDto reference() {
        return referenceService.reference();
    }

    // ---- rules ----

    @GetMapping("/rules")
    public List<TaxRuleDto> rules() {
        return ruleService.list();
    }

    @GetMapping("/rules/{id}")
    public TaxRuleDto rule(@PathVariable Long id) {
        return ruleService.get(id);
    }

    @PostMapping("/rules")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public TaxRuleDto createRule(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveTaxRuleRequest request) {
        return ruleService.create(jwt.getSubject(), request);
    }

    @PutMapping("/rules/{id}")
    public TaxRuleDto updateRule(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody SaveTaxRuleRequest request) {
        return ruleService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/rules/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        ruleService.delete(jwt.getSubject(), id);
    }

    @PostMapping("/rules/{id}/submit")
    public TaxRuleDto submit(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ruleService.submit(jwt.getSubject(), id);
    }

    @PostMapping("/rules/{id}/approve")
    public TaxRuleDto approve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ruleService.approve(jwt.getSubject(), id);
    }

    @PostMapping("/rules/{id}/reject")
    public TaxRuleDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ruleService.reject(jwt.getSubject(), id, request.reason());
    }

    @PostMapping("/rules/{id}/activate")
    public TaxRuleDto activate(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ruleService.activate(jwt.getSubject(), id);
    }

    @PostMapping("/rules/{id}/deactivate")
    public TaxRuleDto deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ruleService.deactivate(jwt.getSubject(), id);
    }

    @PostMapping("/rules/{id}/new-version")
    public TaxRuleDto newVersion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ruleService.newVersion(jwt.getSubject(), id);
    }

    @PostMapping("/preview")
    public TaxPreviewDto preview(@Valid @RequestBody TaxPreviewRequest request) {
        return ruleService.preview(request);
    }

    // ---- tax classes and product classification ----

    @GetMapping("/classes")
    public List<TaxClassDto> classes() {
        return classService.list();
    }

    @PostMapping("/classes")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public TaxClassDto createClass(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveTaxClassRequest request) {
        return classService.create(jwt.getSubject(), request);
    }

    @PutMapping("/classes/{id}")
    public TaxClassDto updateClass(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody SaveTaxClassRequest request) {
        return classService.update(jwt.getSubject(), id, request);
    }

    @GetMapping("/products")
    public List<ProductTaxClassDto> products() {
        return classService.products();
    }

    @PutMapping("/products/{productId}/class")
    public ProductTaxClassDto assignClass(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @Valid @RequestBody AssignTaxClassRequest request) {
        return classService.assign(jwt.getSubject(), productId, request);
    }

    // ---- supplier registrations ----

    @GetMapping("/registrations")
    public List<TaxRegistrationDto> registrations() {
        return registrationService.list();
    }

    @PostMapping("/registrations")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public TaxRegistrationDto createRegistration(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveTaxRegistrationRequest request) {
        return registrationService.create(jwt.getSubject(), request);
    }

    @PutMapping("/registrations/{id}")
    public TaxRegistrationDto updateRegistration(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody SaveTaxRegistrationRequest request) {
        return registrationService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/registrations/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRegistration(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        registrationService.delete(jwt.getSubject(), id);
    }

    // ---- exemptions ----

    @GetMapping("/exemptions")
    public List<TaxExemptionDto> exemptions() {
        return exemptionService.list();
    }

    @PostMapping("/exemptions")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public TaxExemptionDto createExemption(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveTaxExemptionRequest request) {
        return exemptionService.create(jwt.getSubject(), request);
    }

    @PostMapping("/exemptions/{id}/approve")
    public TaxExemptionDto approveExemption(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return exemptionService.approve(jwt.getSubject(), id);
    }

    @PostMapping("/exemptions/{id}/revoke")
    public TaxExemptionDto revokeExemption(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return exemptionService.revoke(jwt.getSubject(), id);
    }

    // ---- Draft invoices (tax could not be determined) ----

    @GetMapping("/blocked-invoices")
    public List<BlockedInvoiceDto> blockedInvoices() {
        return invoiceService.listBlocked();
    }

    /** Retry tax: restarts the calculation with the engine in force now. Returns the invoice, still a Draft or now issued. */
    @PostMapping("/blocked-invoices/{invoiceId}/retry")
    public InvoiceDto retry(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        invoiceService.retryTax(invoiceId, jwt.getSubject());
        return invoiceService.getAdminInvoiceDetail(invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/calculations")
    public List<TaxCalculationDto> calculations(@PathVariable Long invoiceId) {
        return calculationService.listForInvoice(invoiceId);
    }
}
