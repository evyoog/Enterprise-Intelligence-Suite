package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceLineDto;
import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceLine;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** REQ-BIL-001.2/.3/.4. Invoice generation is a side effect called from
 * {@code SubscriptionService#subscribe}/{@code #subscribeOrganization}/
 * {@code #renewSubscription} — never the other way around (this module
 * never changes when or whether a subscription activates; FRD Open question
 * 3 on that is still open, decision C47). */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BillingDetailsRepository billingDetailsRepository;
    private final BillingDetailsService billingDetailsService;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final BillingSettingsService billingSettingsService;

    /** Called after a subscription is created or renewed. Silently does
     * nothing for a zero-amount plan (nothing to bill) — most of this
     * platform's seeded catalog is $0 "Standard" plans (decision C38), so an
     * invoice would be pure noise there. */
    @Transactional
    public Invoice generateForSubscription(Long subscriptionId) {
        return generateForSubscriptions(List.of(subscriptionId));
    }

    /** C59 (REQ-MKT-003.8, engineering default for REQ-MKT-003 Open
     * question 1): one invoice for a whole cart, one line per subscription.
     * Every subscription must have the same owner and currency. Zero-amount
     * lines are skipped; returns null when nothing is left to bill. The
     * invoice's own {@code subscriptionId} is the first billed subscription
     * (each line carries its own). */
    @Transactional
    public Invoice generateForSubscriptions(List<Long> subscriptionIds) {
        record Billed(ProductSubscription subscription, Product product, ProductPlan plan, long amount, Currency currency) {}
        List<Billed> billed = new ArrayList<>();
        for (Long subscriptionId : subscriptionIds) {
            ProductSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
            Product product = productRepository.findById(subscription.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            ProductPlan plan = subscription.getPlanId() == null ? null
                : productPlanRepository.findById(subscription.getPlanId()).orElse(null);
            BigDecimal price = plan != null ? plan.getPrice() : product.getPrice();
            Currency currency = plan != null ? plan.getCurrency() : Currency.USD;
            long amount = toMinorUnits(price);
            if (amount > 0) {
                billed.add(new Billed(subscription, product, plan, amount, currency));
            }
        }
        if (billed.isEmpty()) {
            return null;
        }
        ProductSubscription first = billed.get(0).subscription();
        Currency currency = billed.get(0).currency();
        for (Billed b : billed) {
            if (b.currency() != currency) {
                throw new IllegalArgumentException("Items in different currencies cannot be billed on one invoice.");
            }
            if (!java.util.Objects.equals(b.subscription().getOwnerCustomerId(), first.getOwnerCustomerId())
                    || !java.util.Objects.equals(b.subscription().getOwnerOrganizationId(), first.getOwnerOrganizationId())) {
                throw new IllegalArgumentException("Subscriptions with different owners cannot be billed on one invoice.");
            }
        }
        long total = billed.stream().mapToLong(Billed::amount).sum();

        Invoice invoice = new Invoice();
        invoice.setSubscriptionId(first.getId());
        invoice.setOwnerCustomerId(first.getOwnerCustomerId());
        invoice.setOwnerOrganizationId(first.getOwnerOrganizationId());
        invoice.setStatus(InvoiceStatus.OPEN);
        invoice.setCurrency(currency);
        invoice.setSubtotal(total);
        invoice.setTaxAmount(0);
        invoice.setTotal(total);
        invoice.setPeriodStart(first.getStartedAt() != null ? first.getStartedAt() : Instant.now());
        invoice.setPeriodEnd(first.getExpiresAt());
        BillingDetails billingDetails = first.getOwnerCustomerId() != null
            ? billingDetailsRepository.findByOwnerCustomerId(first.getOwnerCustomerId()).orElse(null)
            : billingDetailsRepository.findByOwnerOrganizationId(first.getOwnerOrganizationId()).orElse(null);
        invoice.setBillToSnapshot(billingDetailsService.snapshotFor(billingDetails));
        invoice = invoiceRepository.save(invoice);

        for (Billed b : billed) {
            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setDescription(b.product().getName() + (b.plan() != null ? " — " + b.plan().getName() : ""));
            line.setPeriodStart(b.subscription().getStartedAt() != null ? b.subscription().getStartedAt() : invoice.getPeriodStart());
            line.setPeriodEnd(b.subscription().getExpiresAt());
            line.setQuantity(1);
            line.setUnitAmount(b.amount());
            line.setAmount(b.amount());
            line.setSubscriptionId(b.subscription().getId());
            invoice.getLines().add(line);
        }

        // C60: due date = issue date + the payment terms set in Billing settings (0 = due on issue, as C47).
        var settings = billingSettingsService.current();
        Instant issued = invoice.getIssuedAt() != null ? invoice.getIssuedAt() : Instant.now();
        invoice.setDueAt(issued.plus(settings.getPaymentTermsDays(), java.time.temporal.ChronoUnit.DAYS));
        invoice = invoiceRepository.save(invoice);
        invoice.setInvoiceNumber(formatInvoiceNumber(invoice, settings.getInvoicePrefix()));
        invoice = invoiceRepository.save(invoice);

        String productNames = String.join(", ", billed.stream().map(b -> b.product().getName()).toList());
        notifyOwner(invoice, productNames);
        auditService.recordSuccess("INVOICE_GENERATED", null, invoice.getOwnerCustomerId(), null,
            "Invoice", invoice.getId().toString(), invoice.getOwnerOrganizationId(),
            "Invoice " + invoice.getInvoiceNumber() + " generated for " + productNames);
        return invoice;
    }

    /** Decision C47: no invoice-number format was specified in the FRD —
     * this pass uses {@code INV-<year>-<id, zero-padded to 6>}, unique and
     * immutable once set (BR-3), easy to read, and never needs a separate
     * per-year counter since the id itself is already unique. */
    private String formatInvoiceNumber(Invoice invoice, String prefix) {
        int year = invoice.getIssuedAt().atZone(ZoneOffset.UTC).getYear();
        // C60: the prefix is set in Billing settings (default "INV"); numbers already issued never change.
        String p = prefix == null || prefix.isBlank() ? "INV" : prefix;
        return p + "-" + year + "-" + String.format("%06d", invoice.getId());
    }

    private long toMinorUnits(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void notifyOwner(Invoice invoice, String productName) {
        if (invoice.getOwnerCustomerId() == null) {
            return;
        }
        customerRepository.findById(invoice.getOwnerCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.BILLING, NotificationSeverity.INFO,
                "Invoice " + invoice.getInvoiceNumber(), "A new invoice for " + productName + " is ready."));
    }

    public Page<InvoiceDto> listForCustomer(Long customerId, Pageable pageable) {
        return invoiceRepository.findByOwnerCustomerIdOrderByIssuedAtDesc(customerId, pageable).map(i -> toDto(i, false, null));
    }

    public Page<InvoiceDto> listForOrganization(Long organizationId, Pageable pageable) {
        return invoiceRepository.findByOwnerOrganizationIdOrderByIssuedAtDesc(organizationId, pageable).map(i -> toDto(i, false, null));
    }

    public Page<InvoiceDto> listForAdmin(Pageable pageable) {
        return invoiceRepository.findAllByOrderByIssuedAtDesc(pageable).map(i -> toDto(i, false, ownerLabel(i)));
    }

    public InvoiceDto getOwnInvoiceDetail(Long customerId, Long organizationId, Long invoiceId) {
        Invoice invoice = resolveOwn(customerId, organizationId, invoiceId);
        return toDto(invoice, true, null);
    }

    public InvoiceDto getAdminInvoiceDetail(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return toDto(invoice, true, ownerLabel(invoice));
    }

    public Invoice resolveOwn(Long customerId, Long organizationId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        boolean owns = (customerId != null && customerId.equals(invoice.getOwnerCustomerId()))
            || (organizationId != null && organizationId.equals(invoice.getOwnerOrganizationId()));
        if (!owns) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        return invoice;
    }

    public Invoice getById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    @Transactional
    public void markPaid(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void markRefundStatus(Invoice invoice, boolean fullyRefunded) {
        invoice.setStatus(fullyRefunded ? InvoiceStatus.REFUNDED : InvoiceStatus.PARTIALLY_REFUNDED);
        invoiceRepository.save(invoice);
    }

    /** Amounts owed right now, across every OPEN invoice, one entry per
     * currency (billing.md Tab 1). */
    public java.util.Map<String, Long> amountDue(Long customerId, Long organizationId) {
        List<Invoice> open = customerId != null
            ? invoiceRepository.findByOwnerCustomerIdAndStatus(customerId, InvoiceStatus.OPEN)
            : invoiceRepository.findByOwnerOrganizationIdAndStatus(organizationId, InvoiceStatus.OPEN);
        return sumByCurrency(open, Invoice::getTotal);
    }

    public java.util.Map<String, Long> spentInPeriod(Long customerId, Long organizationId, Instant from, Instant to) {
        List<Invoice> paid = customerId != null
            ? invoiceRepository.findByOwnerCustomerIdAndStatusIn(customerId, List.of(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_REFUNDED))
            : invoiceRepository.findByOwnerOrganizationIdAndStatusIn(organizationId, List.of(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_REFUNDED));
        List<Invoice> inRange = paid.stream()
            .filter(i -> !i.getIssuedAt().isBefore(from) && i.getIssuedAt().isBefore(to))
            .toList();
        return sumByCurrency(inRange, Invoice::getTotal);
    }

    /** Platform admin dashboard (C53): the same computation as
     * {@link #spentInPeriod}, across every owner instead of one — platform-
     * wide revenue for the business dashboard's own 01.02.01 figure, applied
     * to the whole platform. */
    public java.util.Map<String, Long> platformSpentInPeriod(Instant from, Instant to) {
        List<Invoice> paid = invoiceRepository.findByStatusIn(List.of(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_REFUNDED));
        List<Invoice> inRange = paid.stream()
            .filter(i -> !i.getIssuedAt().isBefore(from) && i.getIssuedAt().isBefore(to))
            .toList();
        return sumByCurrency(inRange, Invoice::getTotal);
    }

    private java.util.Map<String, Long> sumByCurrency(List<Invoice> invoices, java.util.function.ToLongFunction<Invoice> amount) {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (Invoice invoice : invoices) {
            result.merge(invoice.getCurrency().name(), amount.applyAsLong(invoice), Long::sum);
        }
        return result;
    }

    public Optional<Invoice> nextUpcoming(Long customerId, Long organizationId) {
        List<Invoice> open = customerId != null
            ? invoiceRepository.findByOwnerCustomerIdAndStatus(customerId, InvoiceStatus.OPEN)
            : invoiceRepository.findByOwnerOrganizationIdAndStatus(organizationId, InvoiceStatus.OPEN);
        return open.stream().min(java.util.Comparator.comparing(Invoice::getDueAt));
    }

    private String ownerLabel(Invoice invoice) {
        if (invoice.getOwnerCustomerId() != null) {
            return customerRepository.findById(invoice.getOwnerCustomerId())
                .map(c -> c.getFirstName() + " " + c.getLastName()).orElse("Unknown customer");
        }
        return "Organization #" + invoice.getOwnerOrganizationId();
    }

    private InvoiceDto toDto(Invoice invoice, boolean withDetail, String ownerLabel) {
        List<InvoiceLineDto> lines = withDetail
            ? invoice.getLines().stream().map(l -> new InvoiceLineDto(l.getDescription(), l.getPeriodStart(), l.getPeriodEnd(),
                l.getQuantity(), l.getUnitAmount(), l.getAmount())).toList()
            : null;
        List<com.vyoog.eisplatform.modules.billing.dto.PaymentDto> payments = withDetail
            ? paymentRepository.findByInvoiceId(invoice.getId()).stream().map(this::toBarePaymentDto).toList()
            : null;
        return new InvoiceDto(invoice.getId(), invoice.getInvoiceNumber(), invoice.getStatus(), invoice.getCurrency(),
            invoice.getSubtotal(), invoice.getTaxAmount(), invoice.getTotal(), invoice.getPeriodStart(), invoice.getPeriodEnd(),
            invoice.getIssuedAt(), invoice.getDueAt(), invoice.getBillToSnapshot(), invoice.getPaymentRoute(), ownerLabel, lines, payments);
    }

    private com.vyoog.eisplatform.modules.billing.dto.PaymentDto toBarePaymentDto(com.vyoog.eisplatform.modules.billing.model.Payment p) {
        return new com.vyoog.eisplatform.modules.billing.dto.PaymentDto(p.getId(), p.getInvoiceId(), null, null, p.getStatus(),
            p.getCurrency(), p.getAmount(), p.getRefundedAmount(), p.getMethodType(), p.getMethodNetwork(), p.getMethodLast4(),
            p.getProviderPaymentId(), p.getFailureReason(), p.getCreatedAt(), p.getCapturedAt(), null, null, p.getOfflineReference());
    }
}
