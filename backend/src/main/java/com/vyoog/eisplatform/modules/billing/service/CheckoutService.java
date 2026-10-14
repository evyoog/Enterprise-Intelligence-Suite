package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.CheckoutItemDto;
import com.vyoog.eisplatform.modules.billing.dto.CheckoutSummaryDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineInvoiceResultDto;
import com.vyoog.eisplatform.modules.billing.dto.TaxLineDto;
import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * C55 (REQ-BIL-001.18, .19): the checkout screen's summary and the "Pay by
 * invoice" choice. Exactly one of {@code customerId}/{@code organizationId}
 * is the owner scope, same convention as {@link InvoiceService#resolveOwn}.
 * Nothing here touches card data (BR-BIL-001) — online payment still goes
 * through {@link PaymentService#createPaymentForInvoice} and Razorpay
 * Checkout.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final BillingDetailsRepository billingDetailsRepository;
    private final CustomerRepository customerRepository;
    private final GatewayRoutingService gatewayRouting;
    private final OfflinePaymentService offlinePaymentService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final BillingSettingsService billingSettingsService;

    /** C97 BI-4: Pay by invoice is only for organizations the platform admin has enabled (and only while the global
     * switch in Billing settings is on). Individual customers always pay online. */
    public boolean payByInvoiceAllowed(Long customerId, Long organizationId) {
        return billingSettingsService.payByInvoiceAllowedFor(organizationId);
    }

    @Transactional(readOnly = true)
    public CheckoutSummaryDto summaryForInvoice(Long customerId, Long organizationId, Long invoiceId) {
        Invoice invoice = invoiceService.resolveOwn(customerId, organizationId, invoiceId);
        ProductSubscription subscription = subscriptionRepository.findById(invoice.getSubscriptionId()).orElse(null);
        return toSummary(customerId, organizationId, invoice, subscription);
    }

    @Transactional(readOnly = true)
    public CheckoutSummaryDto summaryForSubscription(Long customerId, Long organizationId, Long subscriptionId) {
        ProductSubscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        boolean owns = (customerId != null && customerId.equals(subscription.getOwnerCustomerId()))
            || (organizationId != null && organizationId.equals(subscription.getOwnerOrganizationId()));
        if (!owns) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        List<Invoice> invoices = invoiceRepository.findBySubscriptionIdOrderByIssuedAtDescIdDesc(subscriptionId);
        Invoice invoice = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.OPEN).findFirst()
            .orElse(invoices.isEmpty() ? null : invoices.get(0));
        return toSummary(customerId, organizationId, invoice, subscription);
    }

    @Transactional
    public OfflineInvoiceResultDto chooseOffline(Long customerId, Long organizationId, Long actingCustomerId, Long invoiceId) {
        if (!payByInvoiceAllowed(customerId, organizationId)) {
            throw new ForbiddenException("Pay by invoice is not available for your account.");
        }
        Invoice invoice = invoiceService.resolveOwn(customerId, organizationId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BillingConflictException("Only an OPEN invoice can be paid by invoice.");
        }
        invoice.setPaymentRoute(PaymentRoute.OFFLINE);
        invoice = invoiceRepository.save(invoice);
        // C97 BI-1: pay by invoice starts the purchase at once, with the invoice's due date.
        invoiceService.publishActivation(invoice, com.vyoog.eisplatform.modules.billing.event.InvoiceActivationEvent.PAY_BY_INVOICE);

        var bank = offlinePaymentService.bankDetails();
        String billingEmail = billingEmail(customerId, organizationId, actingCustomerId);
        String body = "Invoice " + invoice.getInvoiceNumber() + " for " + money(invoice.getTotal()) + " " + invoice.getCurrency()
            + " is ready. Pay by bank transfer, NEFT/RTGS or cheque, quoting invoice number " + invoice.getInvoiceNumber()
            + " as the payment reference."
            + (bank.isComplete()
                ? " Account name: " + bank.accountName() + ". Bank: " + bank.bankName()
                    + (bank.branchName() != null ? " (" + bank.branchName() + ")" : "") + ". Account number: " + bank.accountNumber()
                    + (bank.ifsc() != null ? ". IFSC: " + bank.ifsc() : "") + (bank.swiftBic() != null ? ". SWIFT/BIC: " + bank.swiftBic() : "")
                    + (bank.iban() != null ? ". IBAN: " + bank.iban() : "") + "."
                : " Bank details are on your invoice.")
            + (bank.upiId() != null ? " UPI ID: " + bank.upiId() + "." : "")
            + (bank.chequeEnabled() && bank.chequePayableTo() != null ? " Cheques payable to " + bank.chequePayableTo()
                + (bank.chequeAddress() != null ? ", sent to " + bank.chequeAddress() : "") + "." : "")
            + (bank.instructions() != null ? " " + bank.instructions() : "");
        notificationService.notify(actingCustomerId, billingEmail, NotificationCategory.BILLING, NotificationSeverity.INFO,
            "Invoice " + invoice.getInvoiceNumber(), body);
        auditService.recordSuccess("INVOICE_PAY_BY_INVOICE_CHOSEN", null, actingCustomerId, null,
            "Invoice", invoice.getId().toString(), invoice.getOwnerOrganizationId(),
            "Pay by invoice chosen for " + invoice.getInvoiceNumber());

        String subscriptionStatus = subscriptionRepository.findById(invoice.getSubscriptionId())
            .map(s -> s.getStatus().name()).orElse(null);
        return new OfflineInvoiceResultDto(invoice.getId(), invoice.getInvoiceNumber(), invoice.getTotal(),
            invoice.getCurrency().name(), invoice.getDueAt(), billingEmail, bank, subscriptionStatus);
    }

    private CheckoutSummaryDto toSummary(Long customerId, Long organizationId, Invoice invoice, ProductSubscription subscription) {
        var settings = billingSettingsService.current();
        Product product = subscription == null ? null : productRepository.findById(subscription.getProductId()).orElse(null);
        ProductPlan plan = subscription == null || subscription.getPlanId() == null ? null
            : productPlanRepository.findById(subscription.getPlanId()).orElse(null);

        List<CheckoutItemDto> items;
        if (invoice != null) {
            // C59: each line names its own subscription (a cart invoice
            // bills several products); older lines fall back to the
            // invoice's single subscription.
            items = invoice.getLines().stream().map(line -> {
                Long lineSubscriptionId = line.getSubscriptionId() != null ? line.getSubscriptionId() : invoice.getSubscriptionId();
                ProductSubscription lineSubscription = subscription != null && subscription.getId().equals(lineSubscriptionId)
                    ? subscription : subscriptionRepository.findById(lineSubscriptionId).orElse(null);
                Product lineProduct = lineSubscription == null ? null
                    : productRepository.findById(lineSubscription.getProductId()).orElse(null);
                ProductPlan linePlan = lineSubscription == null || lineSubscription.getPlanId() == null ? null
                    : productPlanRepository.findById(lineSubscription.getPlanId()).orElse(null);
                return new CheckoutItemDto(
                    lineProduct != null ? lineProduct.getId() : null,
                    lineProduct != null ? lineProduct.getName() : line.getDescription(),
                    lineProduct != null ? lineProduct.getImageUrl() : null,
                    linePlan != null ? linePlan.getName() : null,
                    linePlan != null ? linePlan.getBillingPeriod().name() : null,
                    line.getPeriodStart(), line.getPeriodEnd(), line.getAmount(), null);
            }).toList();
        } else if (product != null) {
            items = List.of(new CheckoutItemDto(product.getId(), product.getName(), product.getImageUrl(),
                plan != null ? plan.getName() : null, plan != null ? plan.getBillingPeriod().name() : null,
                subscription.getStartedAt(), subscription.getExpiresAt(), 0, null));
        } else {
            items = List.of();
        }

        // C96: the components copied onto the invoice, summed over its lines per component and rate.
        List<TaxLineDto> taxLines = invoice == null ? List.of()
            : invoiceService.taxLinesOf(invoice.getId()).stream()
                .filter(t -> t.taxAmount() > 0 || t.rate() != null)
                .collect(java.util.stream.Collectors.groupingBy(t -> t.code() + "|" + t.rate(), java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()))
                .values().stream().map(group -> new TaxLineDto(group.get(0).name(), group.get(0).rate(),
                    group.stream().mapToLong(t -> t.taxAmount()).sum())).toList();
        String currency = invoice != null ? invoice.getCurrency().name()
            : plan != null ? plan.getCurrency().name() : "USD";
        var preferred = gatewayRouting.preferred(currency).orElse(null);

        return new CheckoutSummaryDto(
            invoice != null ? invoice.getId() : null,
            invoice != null ? invoice.getInvoiceNumber() : null,
            invoice != null ? invoice.getStatus() : null,
            invoice != null ? invoice.getPaymentRoute() : null,
            subscription != null ? subscription.getId() : null,
            subscription != null ? subscription.getStatus().name() : null,
            currency, items,
            invoice != null ? invoice.getSubtotal() : 0,
            taxLines,
            invoice != null ? invoice.getTotal() : 0,
            invoice != null ? invoice.getDueAt() : null,
            billingEmail(customerId, organizationId, customerId),
            gatewayRouting.available(currency),
            payByInvoiceAllowed(customerId, organizationId),
            offeredMethods(currency),
            settings.getCheckoutDisplayName(),
            settings.getCheckoutDescription(),
            settings.getCheckoutThemeColor(),
            invoice != null && invoice.getStatus() == InvoiceStatus.DRAFT,
            invoice != null ? invoice.getPlaceOfSupply() : null,
            settings.getTermsUrl(), settings.getPrivacyUrl(), settings.getRefundPolicyUrl(),
            invoice != null ? invoice.getPurchaseOrderNumber() : null,
            preferred == null ? null : preferred.code(), preferred == null ? null : preferred.name());
    }

    /** C98: a method is offered only if Billing settings allow it and an eligible gateway offers it (the tile order of Billing settings is kept). */
    private List<String> offeredMethods(String currency) {
        java.util.Set<String> byGateway = gatewayRouting.offeredMethods(currency);
        return billingSettingsService.enabledOnlineMethods().stream().filter(byGateway::contains).toList();
    }

    private String billingEmail(Long customerId, Long organizationId, Long fallbackCustomerId) {
        BillingDetails details = customerId != null
            ? billingDetailsRepository.findByOwnerCustomerId(customerId).orElse(null)
            : billingDetailsRepository.findByOwnerOrganizationId(organizationId).orElse(null);
        if (details != null) {
            return details.getBillingEmail();
        }
        return fallbackCustomerId == null ? null
            : customerRepository.findById(fallbackCustomerId).map(Customer::getEmail).orElse(null);
    }

    private String money(long minorUnits) {
        return BigDecimal.valueOf(minorUnits).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toPlainString();
    }
}
