package com.vyoog.eisplatform.modules.billing.service.razorpay;

/** Everything the billing module needs from Razorpay, kept as a narrow
 * interface (not the full Razorpay API) so service-layer tests can supply a
 * fake without any real network call. The real implementation
 * ({@link RazorpayHttpClient}) is only ever invoked when
 * {@link RazorpayProperties#isConfigured()} is true — every service method
 * checks that first and throws {@link PaymentGatewayNotConfiguredException}
 * itself (BR-10), so this interface's methods can assume they're allowed to
 * call out. */
public interface RazorpayClient {

    /** Creates a Razorpay order for the given amount (in the currency's
     * smallest unit) and returns its order id. */
    String createOrder(long amountMinorUnits, String currencyCode, String receipt);

    /** Standard Razorpay Checkout signature check:
     * {@code HMAC_SHA256(order_id + "|" + payment_id, key_secret) == signature}. */
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    /** Webhook signature check: {@code HMAC_SHA256(rawBody, webhook_secret) == signatureHeader}. */
    boolean verifyWebhookSignature(String rawBody, String signatureHeader);

    /** Fetches a payment's current method/status details from Razorpay, used
     * both right after a checkout confirm and by the admin Reconcile action. */
    RazorpayPaymentInfo fetchPayment(String paymentId);

    /** Full or partial refund of a captured payment. Returns the Razorpay refund id. */
    String createRefund(String paymentId, long amountMinorUnits, String reason);

    /** Best-effort: deletes a saved card/UPI token at Razorpay. */
    void deleteToken(String providerTokenRef);

    /** One harmless authenticated call, for the admin "Test connection" action. */
    void testConnection();
}
