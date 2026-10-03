package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.Payment;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REQ-INT-002.8 (C62): PaymentAuthorized (captured, online or recorded
 * offline) and PaymentFailed. The payload carries IDs, amount and provider
 * only — no card number, network or last digits (BR-BIL-001).
 */
@Component
@RequiredArgsConstructor
public class PaymentEvents {

    private final OutboxService outboxService;

    public void captured(Payment payment) {
        outboxService.publish(PlatformEventTypes.PAYMENT_AUTHORIZED, PlatformEventTypes.AGGREGATE_PAYMENT, payment.getId(), payload(payment));
    }

    public void failed(Payment payment) {
        outboxService.publish(PlatformEventTypes.PAYMENT_FAILED, PlatformEventTypes.AGGREGATE_PAYMENT, payment.getId(), payload(payment));
    }

    private static Map<String, Object> payload(Payment payment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentId", payment.getId());
        payload.put("invoiceId", payment.getInvoiceId());
        payload.put("provider", payment.getProvider());
        payload.put("currency", payment.getCurrency() == null ? null : payment.getCurrency().name());
        payload.put("amount", payment.getAmount());
        payload.put("status", payment.getStatus() == null ? null : payment.getStatus().name());
        return payload;
    }
}
