package com.vyoog.eisplatform.modules.integration.service;

/** REQ-INT-002.8: the event catalogue. Names reuse the workbook's Events
 * column where one matches (CheckoutCompleted, PaymentAuthorized). */
public final class PlatformEventTypes {

    public static final String CHECKOUT_COMPLETED = "CheckoutCompleted";
    public static final String ORDER_APPROVED = "OrderApproved";
    public static final String SUBSCRIPTION_CREATED = "SubscriptionCreated";
    public static final String SUBSCRIPTION_CHANGED = "SubscriptionChanged";
    public static final String SUBSCRIPTION_SUSPENDED = "SubscriptionSuspended";
    public static final String SUBSCRIPTION_RESUMED = "SubscriptionResumed";
    public static final String SUBSCRIPTION_CANCELLED = "SubscriptionCancelled";
    public static final String SUBSCRIPTION_RENEWED = "SubscriptionRenewed";
    public static final String INVOICE_GENERATED = "InvoiceGenerated";
    public static final String PAYMENT_AUTHORIZED = "PaymentAuthorized";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String SEATS_CHANGED = "SeatsChanged";
    public static final String RENEWAL_REMINDER_SENT = "RenewalReminderSent";

    public static final String AGGREGATE_CART = "Cart";
    public static final String AGGREGATE_ORDER = "Order";
    public static final String AGGREGATE_SUBSCRIPTION = "Subscription";
    public static final String AGGREGATE_INVOICE = "Invoice";
    public static final String AGGREGATE_PAYMENT = "Payment";

    private PlatformEventTypes() {
    }
}
