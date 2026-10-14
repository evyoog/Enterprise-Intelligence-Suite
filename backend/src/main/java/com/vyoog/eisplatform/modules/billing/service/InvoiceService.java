package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceLineDto;
import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceLine;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.InvoiceTaxLine;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceTaxLineRepository;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxComponentDto;
import com.vyoog.eisplatform.modules.billing.tax.model.InvoiceTaxStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxClass;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxCalculationService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxClassService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxRequestAssembler;
import com.vyoog.eisplatform.modules.billing.tax.support.TaxIdValidator;
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
    private final TaxCalculationService taxCalculationService;
    private final TaxRequestAssembler taxRequestAssembler;
    private final TaxClassService taxClassService;
    private final InvoiceTaxLineRepository invoiceTaxLineRepository;
    private final InvoiceNumberService invoiceNumberService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    /** REQ-INT-002 (C62): InvoiceGenerated. */
    private final com.vyoog.eisplatform.modules.integration.service.OutboxService outboxService;

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

        // C96: the invoice is created as a Draft; it is finalized (number, issue date, due date, event) only when its tax
        // has been calculated safely (BR-40).
        Invoice invoice = new Invoice();
        invoice.setSubscriptionId(first.getId());
        invoice.setOwnerCustomerId(first.getOwnerCustomerId());
        invoice.setOwnerOrganizationId(first.getOwnerOrganizationId());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setCurrency(currency);
        invoice.setSubtotal(total);
        invoice.setTaxAmount(0);
        invoice.setTotal(total);
        invoice.setTaxStatus(InvoiceTaxStatus.BLOCKED);
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
            TaxClass taxClass = taxClassService.classOf(b.product());
            line.setTaxClassCode(b.product().getTaxClassCode());
            line.setHsnSacCode(taxClass == null ? null : taxClass.getHsnSacCode());
            invoice.getLines().add(line);
        }
        invoice = invoiceRepository.saveAndFlush(invoice);
        return calculateTaxAndFinalize(invoice);
    }

    /** REQ-BIL-002.31 and .40 to .42: calculate the tax with the engine the switch names NOW (pinned for this whole call), then
     * either finalize the invoice or leave it as a Draft with the reason. Never throws for a tax problem. Used for a new invoice
     * and for "Retry tax" (a retry restarts with the mode in force at that moment, BR-41). */
    private Invoice calculateTaxAndFinalize(Invoice invoice) {
        TaxModeSnapshot pinned = taxCalculationService.pin();
        List<TaxCalculationRequest.Line> lines = invoice.getLines().stream().map(l -> new TaxCalculationRequest.Line(String.valueOf(l.getId()),
            l.getDescription(), l.getTaxClassCode(), l.getHsnSacCode(), l.getAmount(), l.getQuantity())).toList();
        String ref = taxCalculationService.nextTransactionRef(invoice.getId());
        TaxCalculationRequest request = taxRequestAssembler.assemble(ref, TransactionType.SALE, java.time.LocalDate.now(ZoneOffset.UTC),
            invoice.getCurrency().name(), invoice.getOwnerCustomerId(), invoice.getOwnerOrganizationId(), lines);
        TaxCalculationResult result = taxCalculationService.calculate(request, pinned);
        var calculation = taxCalculationService.record(invoice.getId(), request, result);
        invoice.setTaxEngine(result.engine());
        invoice.setTaxConfigVersion(result.configVersion());
        invoice.setTaxCalculationId(calculation.getId());

        if (!result.isCalculated()) {
            invoice.setStatus(InvoiceStatus.DRAFT);
            invoice.setTaxStatus(InvoiceTaxStatus.BLOCKED);
            invoice.setTaxBlockCode(result.blockCode().name());
            invoice.setTaxBlockReason(result.blockMessage() == null ? null : result.blockMessage().length() > 500 ? result.blockMessage().substring(0, 500) : result.blockMessage());
            invoice.setTaxAmount(0);
            invoice.setTotal(invoice.getSubtotal());
            invoice = invoiceRepository.save(invoice);
            auditService.record("INVOICE_TAX_BLOCKED", null, invoice.getOwnerCustomerId(), null, "Invoice", invoice.getId().toString(),
                invoice.getOwnerOrganizationId(), "FAILURE", "Invoice " + invoice.getId() + " is a Draft: " + result.blockCode() + " - " + result.blockMessage());
            return invoice;
        }

        invoiceTaxLineRepository.deleteByInvoiceId(invoice.getId());
        int order = 0;
        for (TaxCalculationResult.LineResult lineResult : result.lines()) {
            InvoiceLine line = invoice.getLines().stream().filter(l -> String.valueOf(l.getId()).equals(lineResult.lineRef())).findFirst().orElseThrow();
            line.setTaxAmount(lineResult.tax());
            for (TaxCalculationResult.Component c : lineResult.components()) {
                InvoiceTaxLine row = new InvoiceTaxLine();
                row.setInvoiceId(invoice.getId());
                row.setInvoiceLineId(line.getId());
                row.setSortOrder(order++);
                row.setComponentCode(c.code());
                row.setComponentName(c.name());
                row.setTaxType(c.taxType());
                row.setJurisdiction(c.jurisdiction());
                row.setTreatment(c.treatment());
                row.setRate(c.rate());
                row.setTaxableAmount(c.taxableAmount());
                row.setTaxAmount(c.taxAmount());
                row.setRuleId(c.ruleId());
                row.setRuleVersion(c.ruleVersion());
                row.setNote(c.note());
                invoiceTaxLineRepository.save(row);
            }
        }
        invoice.setTaxAmount(result.totalTax());
        invoice.setTotal(invoice.getSubtotal() + result.totalTax());
        invoice.setTaxStatus(InvoiceTaxStatus.CALCULATED);
        invoice.setTaxBlockCode(null);
        invoice.setTaxBlockReason(null);
        invoice.setPlaceOfSupply(result.placeOfSupply());
        TaxCalculationRequest.Customer customer = request.customer();
        invoice.setCustomerTaxId(customer.hasTaxId() ? customer.taxId().trim().toUpperCase(java.util.Locale.ROOT) : null);
        return finalizeInvoice(invoice);
    }

    /** BR-40: number, issue date, due date, issuer snapshot, notification, audit and the InvoiceGenerated event. */
    private Invoice finalizeInvoice(Invoice invoice) {
        var settings = billingSettingsService.current();
        Instant issued = Instant.now();
        invoice.setStatus(InvoiceStatus.OPEN);
        invoice.setIssuedAt(issued);
        // C60/BI-2: due date = issue date + the payment terms set in Billing settings (default 15 days).
        invoice.setDueAt(issued.plus(settings.getPaymentTermsDays(), java.time.temporal.ChronoUnit.DAYS));
        invoice.setInvoiceNumber(invoiceNumberService.next(InvoiceNumberService.INVOICE_SERIES, settings.getInvoicePrefix(), issued));
        invoice.setIssuerSnapshot(issuerSnapshot(settings));
        invoice = invoiceRepository.save(invoice);

        String productNames = String.join(", ", invoice.getLines().stream().map(InvoiceLine::getDescription).toList());
        notifyOwner(invoice, productNames);
        auditService.recordSuccess("INVOICE_GENERATED", null, invoice.getOwnerCustomerId(), null,
            "Invoice", invoice.getId().toString(), invoice.getOwnerOrganizationId(),
            "Invoice " + invoice.getInvoiceNumber() + " generated for " + productNames + " (tax " + invoice.getTaxAmount() + ", " + invoice.getTaxEngine() + " engine)");
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("invoiceId", invoice.getId());
        payload.put("invoiceNumber", invoice.getInvoiceNumber());
        payload.put("ownerCustomerId", invoice.getOwnerCustomerId());
        payload.put("ownerOrganizationId", invoice.getOwnerOrganizationId());
        payload.put("subscriptionIds", invoice.getLines().stream().map(InvoiceLine::getSubscriptionId).filter(java.util.Objects::nonNull).toList());
        payload.put("currency", invoice.getCurrency().name());
        payload.put("subtotal", invoice.getSubtotal());
        payload.put("taxAmount", invoice.getTaxAmount());
        payload.put("total", invoice.getTotal());
        payload.put("dueAt", invoice.getDueAt() == null ? null : invoice.getDueAt().toString());
        outboxService.publish(com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes.INVOICE_GENERATED,
            com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes.AGGREGATE_INVOICE, invoice.getId(), payload);
        return invoice;
    }

    /** BI-3: the issuer's legal name, address, GSTIN and PAN as they are when the invoice is finalized. */
    private String issuerSnapshot(com.vyoog.eisplatform.modules.billing.model.BillingSettings p) {
        if (p.getBusinessLegalName() == null || p.getBusinessLegalName().isBlank()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(p.getBusinessLegalName());
        java.util.function.BiConsumer<String, String> add = (label, value) -> {
            if (value != null && !value.isBlank()) {
                sb.append('\n').append(label).append(value);
            }
        };
        add.accept("", String.join(", ", java.util.stream.Stream.of(p.getBusinessAddressLine1(), p.getBusinessAddressLine2(), p.getBusinessCity(),
            p.getBusinessState(), p.getBusinessPostalCode(), p.getBusinessCountry()).filter(v -> v != null && !v.isBlank()).toList()));
        add.accept("GSTIN: ", p.getBusinessGstin());
        add.accept("PAN: ", p.getBusinessPan());
        add.accept("CIN: ", p.getBusinessCin());
        String text = sb.toString();
        return text.length() > 1500 ? text.substring(0, 1500) : text;
    }

    /** REQ-BIL-002.42: a platform administrator presses "Retry tax" on a Draft after fixing the cause. */
    @Transactional
    public Invoice retryTax(Long invoiceId, String actorSub) {
        Invoice invoice = getById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new com.vyoog.eisplatform.common.exception.BillingConflictException("Only a Draft invoice can have its tax recalculated. An issued invoice never changes.");
        }
        Invoice result = calculateTaxAndFinalize(invoice);
        auditService.recordSuccess("INVOICE_TAX_RETRIED", actorSub, null, null, "Invoice", invoiceId.toString(), invoice.getOwnerOrganizationId(),
            "Tax recalculated for Draft invoice " + invoiceId + ": " + (result.getStatus() == InvoiceStatus.DRAFT ? "still blocked (" + result.getTaxBlockCode() + ")" : "invoice " + result.getInvoiceNumber() + " issued"));
        return result;
    }

    /** REQ-BIL-002.42: the Drafts waiting for tax, oldest first, for Tax Configuration. */
    @Transactional(readOnly = true)
    public List<com.vyoog.eisplatform.modules.billing.tax.dto.BlockedInvoiceDto> listBlocked() {
        return invoiceRepository.findByStatusOrderByIdAsc(InvoiceStatus.DRAFT).stream().map(i -> new com.vyoog.eisplatform.modules.billing.tax.dto.BlockedInvoiceDto(
            i.getId(), ownerLabel(i), i.getCurrency().name(), i.getSubtotal(), i.getIssuedAt(), i.getTaxBlockCode(), i.getTaxBlockReason(),
            i.getTaxEngine() == null ? null : i.getTaxEngine().name())).toList();
    }

    /** After a customer or organization saves its billing details, its Draft invoices are tried again. */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int retryDraftsForOwner(Long customerId, Long organizationId) {
        List<Invoice> drafts = customerId != null
            ? invoiceRepository.findByOwnerCustomerIdAndStatusOrderByIdAsc(customerId, InvoiceStatus.DRAFT)
            : invoiceRepository.findByOwnerOrganizationIdAndStatusOrderByIdAsc(organizationId, InvoiceStatus.DRAFT);
        int issued = 0;
        for (Invoice draft : drafts) {
            if (calculateTaxAndFinalize(draft).getStatus() != InvoiceStatus.DRAFT) {
                issued++;
            }
        }
        return issued;
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
        // C96: a Draft (tax not determined) is not an invoice yet; customers do not see it in their list.
        return invoiceRepository.findByOwnerCustomerIdAndStatusNotOrderByIssuedAtDesc(customerId, InvoiceStatus.DRAFT, pageable).map(i -> toDto(i, false, null, false));
    }

    public Page<InvoiceDto> listForOrganization(Long organizationId, Pageable pageable) {
        return invoiceRepository.findByOwnerOrganizationIdAndStatusNotOrderByIssuedAtDesc(organizationId, InvoiceStatus.DRAFT, pageable).map(i -> toDto(i, false, null, false));
    }

    public Page<InvoiceDto> listForAdmin(Pageable pageable) {
        return invoiceRepository.findAllByOrderByIssuedAtDesc(pageable).map(i -> toDto(i, false, ownerLabel(i), true));
    }

    @Transactional(readOnly = true)
    public InvoiceDto getOwnInvoiceDetail(Long customerId, Long organizationId, Long invoiceId) {
        Invoice invoice = resolveOwn(customerId, organizationId, invoiceId);
        return toDto(invoice, true, null, false);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getAdminInvoiceDetail(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return toDto(invoice, true, ownerLabel(invoice), true);
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

    /** C97 BI-9: an optional purchase-order number the customer can add (or clear) until the invoice is paid. */
    @Transactional
    public InvoiceDto setPurchaseOrder(Long customerId, Long organizationId, Long invoiceId, String purchaseOrderNumber) {
        Invoice invoice = resolveOwn(customerId, organizationId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN && invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new com.vyoog.eisplatform.common.exception.BillingConflictException("A purchase-order number can only be changed before the invoice is paid.");
        }
        invoice.setPurchaseOrderNumber(purchaseOrderNumber == null || purchaseOrderNumber.isBlank() ? null : purchaseOrderNumber.trim());
        invoice = invoiceRepository.save(invoice);
        auditService.recordSuccess("INVOICE_PO_NUMBER_SET", null, customerId, null, "Invoice", invoice.getId().toString(),
            invoice.getOwnerOrganizationId(), "Purchase-order number " + (invoice.getPurchaseOrderNumber() == null ? "cleared" : "set") + " on " + invoice.getInvoiceNumber());
        return toDto(invoice, true, null, false);
    }

    public Invoice getById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    @Transactional
    public void markPaid(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        publishActivation(invoice, com.vyoog.eisplatform.modules.billing.event.InvoiceActivationEvent.PAID);
    }

    /** C97 BI-1: tells the subscription module that the purchases on this invoice may start. */
    public void publishActivation(Invoice invoice, String reason) {
        List<Long> subscriptionIds = new ArrayList<>(invoice.getLines().stream().map(InvoiceLine::getSubscriptionId)
            .filter(java.util.Objects::nonNull).distinct().toList());
        if (subscriptionIds.isEmpty() && invoice.getSubscriptionId() != null) {
            subscriptionIds.add(invoice.getSubscriptionId());
        }
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.billing.event.InvoiceActivationEvent(invoice.getId(), subscriptionIds, reason));
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

    private InvoiceDto toDto(Invoice invoice, boolean withDetail, String ownerLabel, boolean admin) {
        List<InvoiceLineDto> lines = withDetail
            ? invoice.getLines().stream().map(l -> new InvoiceLineDto(l.getDescription(), l.getPeriodStart(), l.getPeriodEnd(),
                l.getQuantity(), l.getUnitAmount(), l.getAmount(), l.getTaxAmount(), l.getTaxClassCode(), l.getHsnSacCode())).toList()
            : null;
        List<com.vyoog.eisplatform.modules.billing.dto.PaymentDto> payments = withDetail
            ? paymentRepository.findByInvoiceId(invoice.getId()).stream().map(this::toBarePaymentDto).toList()
            : null;
        List<TaxComponentDto> taxLines = withDetail ? taxLinesOf(invoice.getId()) : null;
        return new InvoiceDto(invoice.getId(), invoice.getInvoiceNumber(), invoice.getStatus(), invoice.getCurrency(),
            invoice.getSubtotal(), invoice.getTaxAmount(), invoice.getTotal(), invoice.getPeriodStart(), invoice.getPeriodEnd(),
            invoice.getIssuedAt(), invoice.getDueAt(), invoice.getBillToSnapshot(), invoice.getPaymentRoute(), ownerLabel, lines, payments,
            invoice.getTaxStatus().name(), invoice.getTaxEngine() == null ? null : invoice.getTaxEngine().name(), invoice.getPlaceOfSupply(),
            invoice.getCustomerTaxId(), withDetail ? invoice.getIssuerSnapshot() : null, taxLines,
            admin ? invoice.getTaxBlockCode() : null, admin ? invoice.getTaxBlockReason() : null,
            invoice.getPurchaseOrderNumber());
    }

    /** The tax components copied onto an invoice when it was calculated (REQ-BIL-002.43), summed per component. */
    public List<TaxComponentDto> taxLinesOf(Long invoiceId) {
        return invoiceTaxLineRepository.findByInvoiceIdOrderBySortOrderAscIdAsc(invoiceId).stream()
            .map(t -> new TaxComponentDto(t.getComponentCode(), t.getComponentName(), t.getTaxType() == null ? null : t.getTaxType().name(), t.getJurisdiction(),
                t.getTreatment().name(), t.getRate(), t.getTaxableAmount(), t.getTaxAmount(), t.getNote())).toList();
    }

    private com.vyoog.eisplatform.modules.billing.dto.PaymentDto toBarePaymentDto(com.vyoog.eisplatform.modules.billing.model.Payment p) {
        return new com.vyoog.eisplatform.modules.billing.dto.PaymentDto(p.getId(), p.getInvoiceId(), null, null, p.getStatus(),
            p.getCurrency(), p.getAmount(), p.getRefundedAmount(), p.getMethodType(), p.getMethodNetwork(), p.getMethodLast4(),
            p.getProviderPaymentId(), p.getFailureReason(), p.getCreatedAt(), p.getCapturedAt(), null, null, p.getOfflineReference());
    }
}
