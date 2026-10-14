package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.InvalidPaymentSignatureException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.*;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.Payment;
import com.vyoog.eisplatform.modules.billing.model.PaymentStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentWebhookEvent;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRefundRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentWebhookEventRepository;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayOrder;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayRegistry;
import com.vyoog.eisplatform.modules.billing.service.gateway.PaymentGateway;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayPaymentInfo;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayWebhookEvent;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** REQ-BIL-001.5-.9, .12, REQ-BIL-003 (C98). Payments are provider-independent: a new payment is routed by the backend to an eligible gateway
 * ({@link GatewayRoutingService}, BR-10 when none is), and every later step (confirm, reconcile, refund, webhook) goes to the provider that
 * created the payment, so changing the active or default gateway never touches payments already made. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final InvoiceService invoiceService;
    private final CreditNoteService creditNoteService;
    private final GatewayRegistry registry;
    private final GatewayRoutingService routing;
    private final GatewayManagementService gatewayManagement;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PaymentEvents paymentEvents;

    @Transactional
    public CreatePaymentResponse createPaymentForInvoice(Long customerId, Long organizationId, Long invoiceId) {
        return createPaymentForInvoice(customerId, organizationId, invoiceId, null);
    }

    /** {@code method} is the checkout tile the customer chose (card, upi, netbanking, wallet), or null. */
    @Transactional
    public CreatePaymentResponse createPaymentForInvoice(Long customerId, Long organizationId, Long invoiceId, String method) {
        Invoice invoice = invoiceService.resolveOwn(customerId, organizationId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BillingConflictException("Only an OPEN invoice can be paid.");
        }
        // C55: the customer's latest choice of route — paying online after
        // first choosing Pay by invoice switches the invoice back to ONLINE.
        invoice.setPaymentRoute(com.vyoog.eisplatform.modules.billing.model.PaymentRoute.ONLINE);
        GatewayRoutingService.Route route = routing.select(invoice.getCurrency().name(), method);   // BR-10: refuses when no gateway can take it
        Payment payment = new Payment();
        payment.setProvider(route.code());
        payment.setInvoiceId(invoice.getId());
        payment.setCurrency(invoice.getCurrency());
        payment.setAmount(invoice.getTotal());
        payment.setStatus(PaymentStatus.CREATED);
        GatewayOrder order = route.adapter().createOrder(route.context(), invoice.getTotal(), invoice.getCurrency().name(), "invoice-" + invoice.getId());
        payment.setProviderOrderId(order.orderId());
        payment = paymentRepository.save(payment);
        return new CreatePaymentResponse(payment.getId(), order.orderId(), payment.getAmount(), payment.getCurrency().name(),
            order.clientConfig().getOrDefault("keyId", ""), payment.getProvider(), order.clientConfig());
    }

    @Transactional
    public PaymentDto confirmPayment(Long customerId, Long organizationId, Long paymentId, ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        invoiceService.resolveOwn(customerId, organizationId, payment.getInvoiceId());
        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new BillingConflictException("This payment has already been processed.");
        }
        GatewayRoutingService.Route route = routing.forProvider(payment.getProvider());
        if (!payment.getProviderOrderId().equals(request.providerOrderId())
                || !route.adapter().verifyPayment(route.context(), request.providerOrderId(), request.providerPaymentId(), request.signature())) {
            throw new InvalidPaymentSignatureException();
        }
        GatewayPaymentInfo info = route.adapter().fetchPayment(route.context(), request.providerPaymentId());
        payment.setProviderPaymentId(request.providerPaymentId());
        payment.setMethodType(info.methodType());
        payment.setMethodNetwork(info.methodNetwork());
        payment.setMethodLast4(info.methodLast4());

        Invoice invoice = invoiceService.getById(payment.getInvoiceId());
        if (info.status() == GatewayPaymentInfo.Status.CAPTURED) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            paymentRepository.save(payment);
            invoiceService.markPaid(invoice);
            notify(invoice, "Payment received", "Invoice " + invoice.getInvoiceNumber() + " is paid.");
            auditService.recordSuccess("PAYMENT_CAPTURED", null, invoice.getOwnerCustomerId(), null,
                "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(), "Payment captured for invoice " + invoice.getInvoiceNumber());
            paymentEvents.captured(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(info.failureReason());
            paymentRepository.save(payment);
            auditService.recordSuccess("PAYMENT_FAILED", null, invoice.getOwnerCustomerId(), null,
                "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(), "Payment failed for invoice " + invoice.getInvoiceNumber());
            paymentEvents.failed(payment);
        }
        return toDto(payment, invoice, null);
    }

    /** The Razorpay webhook as it was before C98, kept so existing callers and tests keep working. */
    @Transactional
    public void processWebhook(String rawBody, String signatureHeader) {
        processWebhook("RAZORPAY", rawBody, signatureHeader == null ? Map.of() : Map.of("X-Razorpay-Signature", signatureHeader));
    }

    /** BR-6/BR-5, REQ-BIL-003.10: a webhook is trusted only once the provider's adapter verifies its signature against the raw body, and is processed
     * at most once per provider and event id. Accepted for any configured provider, even a deactivated one, so payments in flight still complete. */
    @Transactional
    public void processWebhook(String provider, String rawBody, Map<String, String> headers) {
        String code = GatewayRegistry.normalise(provider);
        PaymentGateway adapter = registry.adapter(code).orElseThrow(() -> new ResourceNotFoundException("Unknown payment provider"));
        if (!gatewayManagement.configured(code)) {
            gatewayManagement.recordWebhookFailure(code, "A webhook arrived but the gateway is not configured.");
            throw new InvalidPaymentSignatureException();
        }
        if (!adapter.verifyWebhook(gatewayManagement.context(code), rawBody, headers)) {
            gatewayManagement.recordWebhookFailure(code, "The webhook signature did not verify.");
            throw new InvalidPaymentSignatureException();
        }
        GatewayWebhookEvent parsed;
        try {
            parsed = adapter.parseWebhook(rawBody);
        } catch (IllegalArgumentException e) {
            gatewayManagement.recordWebhookFailure(code, "The webhook body was not understood.");
            throw new InvalidPaymentSignatureException();
        }
        if (webhookEventRepository.findByProviderAndProviderEventId(code, parsed.eventId()).isPresent()) {
            return; // already processed — acknowledge without reprocessing (BR-6)
        }

        PaymentWebhookEvent event = new PaymentWebhookEvent();
        event.setProvider(code);
        event.setProviderEventId(parsed.eventId());
        event.setEventType(parsed.eventType());
        event.setPayloadSummary(parsed.eventType());

        switch (parsed.type()) {
            case PAYMENT_CAPTURED -> handlePaymentCaptured(code, parsed, event);
            case PAYMENT_FAILED -> handlePaymentFailed(code, parsed, event);
            case REFUND_PROCESSED -> handleRefundProcessed(code, parsed, event);
            default -> { /* no other event types are subscribed to (docs/09-integrations/razorpay.md) */ }
        }
        event.setProcessedAt(Instant.now());
        webhookEventRepository.save(event);
        gatewayManagement.recordWebhookReceived(code, parsed.eventType());
    }

    private void handlePaymentCaptured(String provider, GatewayWebhookEvent e, PaymentWebhookEvent event) {
        if (e.providerOrderId() == null) return;
        paymentRepository.findByProviderAndProviderOrderId(provider, e.providerOrderId()).ifPresent(payment -> {
            event.setPaymentId(payment.getId());
            if (payment.getStatus() == PaymentStatus.CAPTURED) return;
            payment.setProviderPaymentId(e.providerPaymentId());
            payment.setMethodType(e.methodType());
            payment.setMethodNetwork(e.methodNetwork());
            payment.setMethodLast4(e.methodLast4());
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            paymentRepository.save(payment);
            invoiceService.markPaid(invoiceService.getById(payment.getInvoiceId()));
            paymentEvents.captured(payment);
        });
    }

    private void handlePaymentFailed(String provider, GatewayWebhookEvent e, PaymentWebhookEvent event) {
        if (e.providerOrderId() == null) return;
        paymentRepository.findByProviderAndProviderOrderId(provider, e.providerOrderId()).ifPresent(payment -> {
            event.setPaymentId(payment.getId());
            if (payment.getStatus() == PaymentStatus.CAPTURED) return;
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.failureReason());
            paymentRepository.save(payment);
            paymentEvents.failed(payment);
        });
    }

    private void handleRefundProcessed(String provider, GatewayWebhookEvent e, PaymentWebhookEvent event) {
        if (e.providerPaymentId() == null) return;
        paymentRepository.findFirstByProviderAndProviderPaymentId(provider, e.providerPaymentId()).ifPresent(payment -> event.setPaymentId(payment.getId()));
        // Refund amount/status reconciliation for a refund initiated at the provider directly (outside this admin screen) is Not specified — this pass
        // only records that the event arrived (webhookEventRepository), since every refund this admin screen itself performs already updates the
        // payment synchronously in #refundPayment below.
    }

    @Transactional
    public PaymentDto refundPayment(Long adminCustomerId, Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BillingConflictException("Only a captured payment can be refunded.");
        }
        if (OfflinePaymentService.OFFLINE.equals(payment.getProvider())) {
            // C55: an offline payment was never taken through Razorpay, so
            // there is nothing there to refund; how offline refunds are
            // handled is Not specified.
            throw new BillingConflictException("Offline payments cannot be refunded through the payment gateway.");
        }
        long refundable = payment.getAmount() - payment.getRefundedAmount();
        if (request.amount() <= 0 || request.amount() > refundable) {
            throw new BillingConflictException("The refund amount must be greater than zero and not more than " + refundable + ".");
        }
        GatewayRoutingService.Route route = routing.forProvider(payment.getProvider());
        String providerRefundId = route.adapter().refund(route.context(), payment.getProviderPaymentId(), request.amount(), request.reason());

        com.vyoog.eisplatform.modules.billing.model.PaymentRefund refund = new com.vyoog.eisplatform.modules.billing.model.PaymentRefund();
        refund.setPaymentId(payment.getId());
        refund.setProviderRefundId(providerRefundId);
        refund.setAmount(request.amount());
        refund.setReason(request.reason());
        refund.setRequestedByCustomerId(adminCustomerId);
        refundRepository.save(refund);

        payment.setRefundedAmount(payment.getRefundedAmount() + request.amount());
        boolean fullyRefunded = payment.getRefundedAmount() >= payment.getAmount();
        payment.setStatus(fullyRefunded ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
        payment = paymentRepository.save(payment);

        Invoice invoice = invoiceService.getById(payment.getInvoiceId());
        invoiceService.markRefundStatus(invoice, fullyRefunded);
        // C97 TX-6: a refund issues a credit note that reverses the invoice's tax in proportion.
        creditNoteService.issueForRefund(invoice, request.amount(), request.reason(), adminCustomerId == null ? null : adminCustomerId.toString(), refund.getId());
        notify(invoice, "Refund processed", "A refund of " + request.amount() + " was processed for invoice " + invoice.getInvoiceNumber() + ".");
        auditService.recordSuccess("PAYMENT_REFUNDED", null, adminCustomerId, null,
            "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(),
            "Refunded " + request.amount() + " (" + request.reason() + ")");
        return toDto(payment, invoice, null);
    }

    @Transactional
    public PaymentDto reconcilePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getProviderPaymentId() == null || OfflinePaymentService.OFFLINE.equals(payment.getProvider())) {
            return toDto(payment, invoiceService.getById(payment.getInvoiceId()), null);
        }
        GatewayRoutingService.Route route = routing.forProvider(payment.getProvider());
        GatewayPaymentInfo info = route.adapter().fetchPayment(route.context(), payment.getProviderPaymentId());
        payment.setMethodType(info.methodType());
        payment.setMethodNetwork(info.methodNetwork());
        payment.setMethodLast4(info.methodLast4());
        if (info.status() == GatewayPaymentInfo.Status.CAPTURED && payment.getStatus() != PaymentStatus.CAPTURED) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            invoiceService.markPaid(invoiceService.getById(payment.getInvoiceId()));
            payment = paymentRepository.save(payment);
            paymentEvents.captured(payment);
        }
        payment = paymentRepository.save(payment);
        return toDto(payment, invoiceService.getById(payment.getInvoiceId()), null);
    }

    public Page<PaymentDto> listForCustomer(Long customerId, Pageable pageable) {
        List<Long> invoiceIds = invoiceService.listForCustomer(customerId, org.springframework.data.domain.Pageable.unpaged())
            .stream().map(InvoiceDto::id).toList();
        return paymentRepository.findByInvoiceIdIn(invoiceIds, pageable).map(p -> toDto(p, invoiceService.getById(p.getInvoiceId()), null));
    }

    public Page<PaymentDto> listForOrganization(Long organizationId, Pageable pageable) {
        List<Long> invoiceIds = invoiceService.listForOrganization(organizationId, org.springframework.data.domain.Pageable.unpaged())
            .stream().map(InvoiceDto::id).toList();
        return paymentRepository.findByInvoiceIdIn(invoiceIds, pageable).map(p -> toDto(p, invoiceService.getById(p.getInvoiceId()), null));
    }

    public Page<PaymentDto> listForAdmin(Pageable pageable) {
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(p -> toDto(p, invoiceService.getById(p.getInvoiceId()), null));
    }

    public PaymentDto getAdminPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        Invoice invoice = invoiceService.getById(payment.getInvoiceId());
        List<RefundDto> refunds = refundRepository.findByPaymentId(paymentId).stream()
            .map(r -> new RefundDto(r.getId(), r.getAmount(), r.getReason(), r.getStatus(), r.getCreatedAt())).toList();
        return toDto(payment, invoice, refunds);
    }

    private void notify(Invoice invoice, String title, String message) {
        if (invoice.getOwnerCustomerId() == null) return;
        customerRepository.findById(invoice.getOwnerCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.BILLING, NotificationSeverity.INFO, title, message));
    }

    private PaymentDto toDto(Payment p, Invoice invoice, List<RefundDto> refunds) {
        return new PaymentDto(p.getId(), p.getInvoiceId(), invoice.getInvoiceNumber(), null, p.getStatus(), p.getCurrency(),
            p.getAmount(), p.getRefundedAmount(), p.getMethodType(), p.getMethodNetwork(), p.getMethodLast4(),
            p.getProviderPaymentId(), p.getFailureReason(), p.getCreatedAt(), p.getCapturedAt(), refunds, null, p.getOfflineReference());
    }
}
