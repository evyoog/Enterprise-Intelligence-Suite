package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.PaymentStatus;
import com.vyoog.eisplatform.modules.product.model.Currency;

import java.time.Instant;
import java.util.List;

/** {@code refunds} and {@code webhookEvents} populated for the admin payment
 * detail panel only (REQ-BIL-001.12), null elsewhere. */
public record PaymentDto(
    Long id,
    Long invoiceId,
    String invoiceNumber,
    String ownerLabel,
    PaymentStatus status,
    Currency currency,
    long amount,
    long refundedAmount,
    String methodType,
    String methodNetwork,
    String methodLast4,
    String providerPaymentId,
    String failureReason,
    Instant createdAt,
    Instant capturedAt,
    List<RefundDto> refunds,
    List<WebhookEventDto> webhookEvents
) {
}
