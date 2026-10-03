package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.dto.PaymentMethodSettingsDto;
import com.vyoog.eisplatform.modules.billing.dto.SaveBusinessProfileRequest;
import com.vyoog.eisplatform.modules.billing.dto.SavePaymentMethodSettingsRequest;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;

import com.vyoog.eisplatform.modules.billing.dto.GatewayStatusDto;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineBankDetailsDto;
import com.vyoog.eisplatform.modules.billing.dto.RecordOfflinePaymentRequest;
import com.vyoog.eisplatform.modules.billing.dto.SaveOfflineBankDetailsRequest;
import com.vyoog.eisplatform.modules.billing.service.OfflinePaymentService;
import com.vyoog.eisplatform.modules.billing.dto.PaymentDto;
import com.vyoog.eisplatform.modules.billing.dto.RefundRequest;
import com.vyoog.eisplatform.modules.billing.service.GatewayStatusService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.service.PaymentService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** REQ-BIL-001.9/.12/.13 — {@code MANAGE_BILLING} (enforced in
 * SecurityConfig, not here — see that class's own convention). */
@RestController
@RequestMapping("/admin/billing")
@RequiredArgsConstructor
public class AdminBillingController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final GatewayStatusService gatewayStatusService;
    private final OfflinePaymentService offlinePaymentService;
    private final BillingSettingsService billingSettingsService;

    @GetMapping("/invoices")
    public Page<InvoiceDto> invoices(@RequestParam(defaultValue = "0") int page) {
        return invoiceService.listForAdmin(PageRequest.of(page, 20));
    }

    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDto invoiceDetail(@PathVariable Long invoiceId) {
        return invoiceService.getAdminInvoiceDetail(invoiceId);
    }

    /** C55 (REQ-BIL-001.20). */
    @PostMapping("/invoices/{invoiceId}/offline-payments")
    public InvoiceDto recordOfflinePayment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId,
                                           @Valid @RequestBody RecordOfflinePaymentRequest request) {
        Customer admin = currentCustomerResolver.resolve(jwt);
        return offlinePaymentService.recordOfflinePayment(admin.getId(), invoiceId, request);
    }

    /** C55 (REQ-BIL-001.21). */
    @GetMapping("/settings/offline")
    public OfflineBankDetailsDto offlineBankDetails() {
        return offlinePaymentService.bankDetails();
    }

    @PutMapping("/settings/offline")
    public OfflineBankDetailsDto saveOfflineBankDetails(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody SaveOfflineBankDetailsRequest request) {
        Customer admin = currentCustomerResolver.resolve(jwt);
        return offlinePaymentService.saveBankDetails(admin.getId(), request);
    }

    /** C60: business profile (invoice issuer) and invoicing rules. */
    /** REQ-SUB-004.4/.5 (C64): platform renewal reminder defaults. */
    @GetMapping("/settings/renewal-reminders")
    public com.vyoog.eisplatform.modules.billing.dto.RenewalReminderDefaultsDto renewalReminderDefaults() {
        return billingSettingsService.renewalReminderDefaults();
    }

    @PutMapping("/settings/renewal-reminders")
    public com.vyoog.eisplatform.modules.billing.dto.RenewalReminderDefaultsDto saveRenewalReminderDefaults(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.vyoog.eisplatform.modules.billing.dto.SaveRenewalReminderDefaultsRequest request) {
        return billingSettingsService.saveRenewalReminderDefaults(currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null), request);
    }

    @GetMapping("/settings/business")
    public BusinessProfileDto businessProfile() {
        return billingSettingsService.businessProfile();
    }

    @PutMapping("/settings/business")
    public BusinessProfileDto saveBusinessProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveBusinessProfileRequest request) {
        Customer admin = currentCustomerResolver.resolve(jwt);
        return billingSettingsService.saveBusinessProfile(admin.getId(), request);
    }

    /** C60: checkout payment methods and Razorpay Checkout appearance (not secrets). */
    @GetMapping("/settings/payment-methods")
    public PaymentMethodSettingsDto paymentMethodSettings() {
        return billingSettingsService.paymentMethods();
    }

    @PutMapping("/settings/payment-methods")
    public PaymentMethodSettingsDto savePaymentMethodSettings(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody SavePaymentMethodSettingsRequest request) {
        Customer admin = currentCustomerResolver.resolve(jwt);
        return billingSettingsService.savePaymentMethods(admin.getId(), request);
    }

    @GetMapping("/payments")
    public Page<PaymentDto> payments(@RequestParam(defaultValue = "0") int page) {
        return paymentService.listForAdmin(PageRequest.of(page, 20));
    }

    @GetMapping("/payments/{paymentId}")
    public PaymentDto paymentDetail(@PathVariable Long paymentId) {
        return paymentService.getAdminPaymentDetail(paymentId);
    }

    @PostMapping("/payments/{paymentId}/refunds")
    public PaymentDto refund(@AuthenticationPrincipal Jwt jwt, @PathVariable Long paymentId, @Valid @RequestBody RefundRequest request) {
        Customer admin = currentCustomerResolver.resolve(jwt);
        return paymentService.refundPayment(admin.getId(), paymentId, request);
    }

    @PostMapping("/payments/{paymentId}/reconcile")
    public PaymentDto reconcile(@PathVariable Long paymentId) {
        return paymentService.reconcilePayment(paymentId);
    }

    @GetMapping("/gateway")
    public GatewayStatusDto gatewayStatus() {
        return gatewayStatusService.getStatus();
    }

    @PostMapping("/gateway/test")
    public void testGateway() {
        gatewayStatusService.testConnection();
    }
}
