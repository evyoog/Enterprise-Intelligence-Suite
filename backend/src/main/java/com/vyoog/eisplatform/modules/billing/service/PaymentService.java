package com.vyoog.eisplatform.modules.billing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.InvalidPaymentSignatureException;
import com.vyoog.eisplatform.common.exception.PaymentGatewayNotConfiguredException;
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
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayPaymentInfo;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
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

/** REQ-BIL-001.5-.9, .12. Every Razorpay-dependent method checks
 * {@link RazorpayProperties#isConfigured()} itself, first (BR-10) — never
 * relies on the controller layer to have checked. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final InvoiceService invoiceService;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public CreatePaymentResponse createPaymentForInvoice(Long customerId, Long organizationId, Long invoiceId) {
        requireConfigured();
        Invoice invoice = invoiceService.resolveOwn(customerId, organizationId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BillingConflictException("Only an OPEN invoice can be paid.");
        }
        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setCurrency(invoice.getCurrency());
        payment.setAmount(invoice.getTotal());
        payment.setStatus(PaymentStatus.CREATED);
        String providerOrderId = razorpayClient.createOrder(invoice.getTotal(), invoice.getCurrency().name(), "invoice-" + invoice.getId());
        payment.setProviderOrderId(providerOrderId);
        payment = paymentRepository.save(payment);
        return new CreatePaymentResponse(payment.getId(), providerOrderId, payment.getAmount(), payment.getCurrency().name(), razorpayProperties.getKeyId());
    }

    @Transactional
    public PaymentDto confirmPayment(Long customerId, Long organizationId, Long paymentId, ConfirmPaymentRequest request) {
        requireConfigured();
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        invoiceService.resolveOwn(customerId, organizationId, payment.getInvoiceId());
        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new BillingConflictException("This payment has already been processed.");
        }
        if (!payment.getProviderOrderId().equals(request.providerOrderId())
                || !razorpayClient.verifyPaymentSignature(request.providerOrderId(), request.providerPaymentId(), request.signature())) {
            throw new InvalidPaymentSignatureException();
        }
        RazorpayPaymentInfo info = razorpayClient.fetchPayment(request.providerPaymentId());
        payment.setProviderPaymentId(request.providerPaymentId());
        payment.setMethodType(info.methodType());
        payment.setMethodNetwork(info.methodNetwork());
        payment.setMethodLast4(info.methodLast4());

        Invoice invoice = invoiceService.getById(payment.getInvoiceId());
        if ("captured".equalsIgnoreCase(info.status()) || "authorized".equalsIgnoreCase(info.status())) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            paymentRepository.save(payment);
            invoiceService.markPaid(invoice);
            notify(invoice, "Payment received", "Invoice " + invoice.getInvoiceNumber() + " is paid.");
            auditService.recordSuccess("PAYMENT_CAPTURED", null, invoice.getOwnerCustomerId(), null,
                "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(), "Payment captured for invoice " + invoice.getInvoiceNumber());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(info.failureReason());
            paymentRepository.save(payment);
            auditService.recordSuccess("PAYMENT_FAILED", null, invoice.getOwnerCustomerId(), null,
                "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(), "Payment failed for invoice " + invoice.getInvoiceNumber());
        }
        return toDto(payment, invoice, null);
    }

    /** BR-6/BR-5: idempotent by Razorpay event id, and only trusted once the
     * webhook signature verifies. */
    @Transactional
    public void processWebhook(String rawBody, String signatureHeader) {
        if (!razorpayProperties.isConfigured() || !razorpayClient.verifyWebhookSignature(rawBody, signatureHeader)) {
            throw new InvalidPaymentSignatureException();
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new InvalidPaymentSignatureException();
        }
        String eventType = root.path("event").asText("unknown");
        JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
        JsonNode refundEntity = root.path("payload").path("refund").path("entity");
        String eventId = root.hasNonNull("id") ? root.path("id").asText()
            : eventType + ":" + (paymentEntity.path("id").asText(refundEntity.path("id").asText("unknown")));
        if (webhookEventRepository.findByProviderEventId(eventId).isPresent()) {
            return; // already processed — acknowledge without reprocessing (BR-6)
        }

        PaymentWebhookEvent event = new PaymentWebhookEvent();
        event.setProviderEventId(eventId);
        event.setEventType(eventType);
        event.setPayloadSummary(eventType);

        switch (eventType) {
            case "payment.captured" -> handlePaymentCaptured(paymentEntity, event);
            case "payment.failed" -> handlePaymentFailed(paymentEntity, event);
            case "refund.processed" -> handleRefundProcessed(refundEntity, event);
            default -> { /* no other event types are subscribed to (docs/09-integrations/razorpay.md) */ }
        }
        event.setProcessedAt(Instant.now());
        webhookEventRepository.save(event);
    }

    private void handlePaymentCaptured(JsonNode paymentEntity, PaymentWebhookEvent event) {
        String orderId = paymentEntity.path("order_id").asText(null);
        if (orderId == null) return;
        paymentRepository.findByProviderOrderId(orderId).ifPresent(payment -> {
            event.setPaymentId(payment.getId());
            if (payment.getStatus() == PaymentStatus.CAPTURED) return;
            payment.setProviderPaymentId(paymentEntity.path("id").asText(null));
            payment.setMethodType(paymentEntity.path("method").asText(null));
            payment.setMethodNetwork(paymentEntity.path("card").path("network").asText(null));
            payment.setMethodLast4(paymentEntity.path("card").path("last4").asText(null));
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            paymentRepository.save(payment);
            invoiceService.markPaid(invoiceService.getById(payment.getInvoiceId()));
        });
    }

    private void handlePaymentFailed(JsonNode paymentEntity, PaymentWebhookEvent event) {
        String orderId = paymentEntity.path("order_id").asText(null);
        if (orderId == null) return;
        paymentRepository.findByProviderOrderId(orderId).ifPresent(payment -> {
            event.setPaymentId(payment.getId());
            if (payment.getStatus() == PaymentStatus.CAPTURED) return;
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(paymentEntity.path("error_description").asText(null));
            paymentRepository.save(payment);
        });
    }

    private void handleRefundProcessed(JsonNode refundEntity, PaymentWebhookEvent event) {
        String providerPaymentId = refundEntity.path("payment_id").asText(null);
        if (providerPaymentId == null) return;
        paymentRepository.findAll().stream()
            .filter(p -> providerPaymentId.equals(p.getProviderPaymentId()))
            .findFirst()
            .ifPresent(payment -> event.setPaymentId(payment.getId()));
        // Refund amount/status reconciliation for a refund initiated at Razorpay
        // directly (outside this admin screen) is Not specified — this pass
        // only records that the event arrived (webhookEventRepository), since
        // every refund this admin screen itself performs already updates the
        // payment synchronously in #refundPayment below.
    }

    @Transactional
    public PaymentDto refundPayment(Long adminCustomerId, Long paymentId, RefundRequest request) {
        requireConfigured();
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BillingConflictException("Only a captured payment can be refunded.");
        }
        long refundable = payment.getAmount() - payment.getRefundedAmount();
        if (request.amount() <= 0 || request.amount() > refundable) {
            throw new BillingConflictException("The refund amount must be greater than zero and not more than " + refundable + ".");
        }
        String providerRefundId = razorpayClient.createRefund(payment.getProviderPaymentId(), request.amount(), request.reason());

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
        notify(invoice, "Refund processed", "A refund of " + request.amount() + " was processed for invoice " + invoice.getInvoiceNumber() + ".");
        auditService.recordSuccess("PAYMENT_REFUNDED", null, adminCustomerId, null,
            "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(),
            "Refunded " + request.amount() + " (" + request.reason() + ")");
        return toDto(payment, invoice, null);
    }

    @Transactional
    public PaymentDto reconcilePayment(Long paymentId) {
        requireConfigured();
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getProviderPaymentId() == null) {
            return toDto(payment, invoiceService.getById(payment.getInvoiceId()), null);
        }
        RazorpayPaymentInfo info = razorpayClient.fetchPayment(payment.getProviderPaymentId());
        payment.setMethodType(info.methodType());
        payment.setMethodNetwork(info.methodNetwork());
        payment.setMethodLast4(info.methodLast4());
        if ("captured".equalsIgnoreCase(info.status()) && payment.getStatus() != PaymentStatus.CAPTURED) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCapturedAt(Instant.now());
            invoiceService.markPaid(invoiceService.getById(payment.getInvoiceId()));
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

    private void requireConfigured() {
        if (!razorpayProperties.isConfigured()) {
            throw new PaymentGatewayNotConfiguredException();
        }
    }

    private void notify(Invoice invoice, String title, String message) {
        if (invoice.getOwnerCustomerId() == null) return;
        customerRepository.findById(invoice.getOwnerCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.BILLING, NotificationSeverity.INFO, title, message));
    }

    private PaymentDto toDto(Payment p, Invoice invoice, List<RefundDto> refunds) {
        return new PaymentDto(p.getId(), p.getInvoiceId(), invoice.getInvoiceNumber(), null, p.getStatus(), p.getCurrency(),
            p.getAmount(), p.getRefundedAmount(), p.getMethodType(), p.getMethodNetwork(), p.getMethodLast4(),
            p.getProviderPaymentId(), p.getFailureReason(), p.getCreatedAt(), p.getCapturedAt(), refunds, null);
    }
}
