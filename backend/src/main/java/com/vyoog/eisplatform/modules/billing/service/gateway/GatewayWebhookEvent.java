package com.vyoog.eisplatform.modules.billing.service.gateway;

/** A provider webhook, translated by its adapter into what EIS needs. {@code eventId} identifies the event for idempotency (per provider). */
public record GatewayWebhookEvent(
    String eventId,
    Type type,
    String eventType,
    String providerOrderId,
    String providerPaymentId,
    String methodType,
    String methodNetwork,
    String methodLast4,
    String failureReason
) {
    public enum Type { PAYMENT_CAPTURED, PAYMENT_FAILED, REFUND_PROCESSED, IGNORED }
}
