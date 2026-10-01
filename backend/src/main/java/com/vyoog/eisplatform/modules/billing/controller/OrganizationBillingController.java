package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.*;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.service.BillingOwnerResolver;
import com.vyoog.eisplatform.modules.billing.service.CheckoutService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.service.PaymentMethodService;
import com.vyoog.eisplatform.modules.billing.service.PaymentService;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** An organization's billing — REQ-BIL-001, "/organization/me" scope. Who
 * may act (organization admins only, via {@code MANAGE_ORGANIZATION}) is
 * enforced by {@link BillingOwnerResolver#requireManageOrganizationBilling}
 * for every write; reads are allowed to any active member of the
 * organization, same as the rest of {@code /organization/me/**}. */
@RestController
@RequestMapping("/organization/me/billing")
@RequiredArgsConstructor
public class OrganizationBillingController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final BillingOwnerResolver ownerResolver;
    private final BillingDetailsService billingDetailsService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;
    private final RazorpayProperties razorpayProperties;
    private final CheckoutService checkoutService;

    @GetMapping("/overview")
    public BillingOverviewResponse overview(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.resolveMembership(customer.getId());
        Long orgId = member.getOrganizationId();
        Instant now = Instant.now();
        var recent = invoiceService.listForOrganization(orgId, PageRequest.of(0, 5)).getContent();
        var next = invoiceService.nextUpcoming(null, orgId).orElse(null);
        return new BillingOverviewResponse(
            invoiceService.amountDue(null, orgId),
            next != null ? next.getDueAt().toString() : null,
            null,
            invoiceService.spentInPeriod(null, orgId, now.minus(30, ChronoUnit.DAYS), now),
            invoiceService.spentInPeriod(null, orgId, now.minus(60, ChronoUnit.DAYS), now.minus(30, ChronoUnit.DAYS)),
            paymentMethodService.defaultForOrganization(orgId),
            recent,
            razorpayProperties.isConfigured()
        );
    }

    /** C55 (REQ-BIL-001.18): read by any active member, same as invoices. */
    @GetMapping("/checkout")
    public CheckoutSummaryDto checkout(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(required = false) Long invoiceId,
                                       @RequestParam(required = false) Long subscriptionId) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        if (invoiceId != null) {
            return checkoutService.summaryForInvoice(null, orgId, invoiceId);
        }
        if (subscriptionId != null) {
            return checkoutService.summaryForSubscription(null, orgId, subscriptionId);
        }
        throw new IllegalArgumentException("invoiceId or subscriptionId is required");
    }

    /** C55 (REQ-BIL-001.19): a write — organization admins only, like Pay. */
    @PostMapping("/invoices/{invoiceId}/offline")
    public OfflineInvoiceResultDto payByInvoice(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        return checkoutService.chooseOffline(null, member.getOrganizationId(), customer.getId(), invoiceId);
    }

    @GetMapping("/details")
    public BillingDetailsDto getDetails(@AuthenticationPrincipal Jwt jwt) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        return billingDetailsService.getForOrganization(orgId);
    }

    @PutMapping("/details")
    public BillingDetailsDto saveDetails(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveBillingDetailsRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        return billingDetailsService.saveForOrganization(customer.getId(), member.getOrganizationId(), request);
    }

    @GetMapping("/invoices")
    public Page<InvoiceDto> invoices(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        return invoiceService.listForOrganization(orgId, PageRequest.of(page, 20));
    }

    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDto invoiceDetail(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        return invoiceService.getOwnInvoiceDetail(null, orgId, invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/document")
    public org.springframework.http.ResponseEntity<byte[]> invoiceDocument(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId, @RequestParam String type) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        var invoice = invoiceService.resolveOwn(null, orgId, invoiceId);
        return InvoiceDocuments.render(invoice, type);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    public CreatePaymentResponse createPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long invoiceId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        return paymentService.createPaymentForInvoice(null, member.getOrganizationId(), invoiceId);
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public PaymentDto confirmPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable Long paymentId, @Valid @RequestBody ConfirmPaymentRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        return paymentService.confirmPayment(null, member.getOrganizationId(), paymentId, request);
    }

    @GetMapping("/payments")
    public Page<PaymentDto> payments(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        return paymentService.listForOrganization(orgId, PageRequest.of(page, 20));
    }

    @GetMapping("/payment-methods")
    public List<PaymentMethodDto> paymentMethods(@AuthenticationPrincipal Jwt jwt) {
        Long orgId = ownerResolver.resolveMembership(currentCustomerResolver.resolve(jwt).getId()).getOrganizationId();
        return paymentMethodService.listForOrganization(orgId);
    }

    @PostMapping("/payment-methods/setup")
    public PaymentMethodSetupResponse setupPaymentMethod(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SetupPaymentMethodRequest request) {
        ownerResolver.requireManageOrganizationBilling(currentCustomerResolver.resolve(jwt).getId());
        return paymentMethodService.startSetup(request);
    }

    @PostMapping("/payment-methods/setup/confirm")
    public PaymentMethodDto confirmPaymentMethodSetup(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfirmPaymentMethodSetupRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        return paymentMethodService.confirmSetup(null, member.getOrganizationId(), request);
    }

    @PostMapping("/payment-methods/{methodId}/default")
    public void setDefaultPaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable Long methodId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        paymentMethodService.setDefault(null, member.getOrganizationId(), methodId);
    }

    @DeleteMapping("/payment-methods/{methodId}")
    public void removePaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable Long methodId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        OrganizationMember member = ownerResolver.requireManageOrganizationBilling(customer.getId());
        paymentMethodService.remove(null, member.getOrganizationId(), methodId);
    }
}
