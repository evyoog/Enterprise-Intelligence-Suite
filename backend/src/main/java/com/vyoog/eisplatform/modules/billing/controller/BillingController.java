package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.*;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.service.PaymentMethodService;
import com.vyoog.eisplatform.modules.billing.service.PaymentService;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** An individual customer's own billing — REQ-BIL-001, "/me" scope. Every
 * method resolves "who is calling" from the JWT, same convention as
 * {@code SubscriptionController}. The organization-billing scope
 * ({@code /organization/me/billing/**}) is a separate controller
 * ({@link OrganizationBillingController}), mirroring how
 * {@code SubscriptionController}/{@code OrderController} are already split
 * by scope rather than one class branching on the URL. */
@RestController
@RequestMapping("/me/billing")
@RequiredArgsConstructor
public class BillingController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final BillingDetailsService billingDetailsService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;
    private final RazorpayProperties razorpayProperties;

    @GetMapping("/overview")
    public BillingOverviewResponse overview(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        Instant now = Instant.now();
        var recent = invoiceService.listForCustomer(customer.getId(), PageRequest.of(0, 5)).getContent();
        var next = invoiceService.nextUpcoming(customer.getId(), null).orElse(null);
        return new BillingOverviewResponse(
            invoiceService.amountDue(customer.getId(), null),
            next != null ? next.getDueAt().toString() : null,
            null,
            invoiceService.spentInPeriod(customer.getId(), null, now.minus(30, ChronoUnit.DAYS), now),
            invoiceService.spentInPeriod(customer.getId(), null, now.minus(60, ChronoUnit.DAYS), now.minus(30, ChronoUnit.DAYS)),
            paymentMethodService.defaultForCustomer(customer.getId()),
            recent,
            razorpayProperties.isConfigured()
        );
    }

    @GetMapping("/details")
    public BillingDetailsDto getDetails(@AuthenticationPrincipal Jwt jwt) {
        return billingDetailsService.getForCustomer(currentCustomerResolver.resolve(jwt).getId());
    }

    @PutMapping("/details")
    public BillingDetailsDto saveDetails(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveBillingDetailsRequest request) {
        return billingDetailsService.saveForCustomer(currentCustomerResolver.resolve(jwt).getId(), request);
    }

    @GetMapping("/invoices")
    public Page<InvoiceDto> invoices(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page) {
        return invoiceService.listForCustomer(currentCustomerResolver.resolve(jwt).getId(), PageRequest.of(page, 20));
    }

    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDto invoiceDetail(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return invoiceService.getOwnInvoiceDetail(customer.getId(), null, invoiceId);
    }

    @GetMapping(value = "/invoices/{invoiceId}/document", produces = MediaType.TEXT_PLAIN_VALUE)
    public org.springframework.http.ResponseEntity<byte[]> invoiceDocument(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId, @RequestParam String type) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        var invoice = invoiceService.resolveOwn(customer.getId(), null, invoiceId);
        return InvoiceDocuments.render(invoice, type);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    public CreatePaymentResponse createPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return paymentService.createPaymentForInvoice(customer.getId(), null, invoiceId);
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public PaymentDto confirmPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long paymentId, @Valid @RequestBody ConfirmPaymentRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return paymentService.confirmPayment(customer.getId(), null, paymentId, request);
    }

    @GetMapping("/payments")
    public Page<PaymentDto> payments(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page) {
        return paymentService.listForCustomer(currentCustomerResolver.resolve(jwt).getId(), PageRequest.of(page, 20));
    }

    @GetMapping("/payment-methods")
    public List<PaymentMethodDto> paymentMethods(@AuthenticationPrincipal Jwt jwt) {
        return paymentMethodService.listForCustomer(currentCustomerResolver.resolve(jwt).getId());
    }

    @PostMapping("/payment-methods/setup")
    public PaymentMethodSetupResponse setupPaymentMethod(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SetupPaymentMethodRequest request) {
        currentCustomerResolver.resolve(jwt);
        return paymentMethodService.startSetup(request);
    }

    @PostMapping("/payment-methods/setup/confirm")
    public PaymentMethodDto confirmPaymentMethodSetup(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfirmPaymentMethodSetupRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return paymentMethodService.confirmSetup(customer.getId(), null, request);
    }

    @PostMapping("/payment-methods/{methodId}/default")
    public void setDefaultPaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable Long methodId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        paymentMethodService.setDefault(customer.getId(), null, methodId);
    }

    @DeleteMapping("/payment-methods/{methodId}")
    public void removePaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable Long methodId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        paymentMethodService.remove(customer.getId(), null, methodId);
    }
}
