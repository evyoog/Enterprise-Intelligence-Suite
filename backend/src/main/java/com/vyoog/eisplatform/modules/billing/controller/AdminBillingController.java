package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.GatewayStatusDto;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
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

    @GetMapping("/invoices")
    public Page<InvoiceDto> invoices(@RequestParam(defaultValue = "0") int page) {
        return invoiceService.listForAdmin(PageRequest.of(page, 20));
    }

    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDto invoiceDetail(@PathVariable Long invoiceId) {
        return invoiceService.getAdminInvoiceDetail(invoiceId);
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
