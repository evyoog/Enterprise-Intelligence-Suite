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

    // REQ-INT-003 platform ↔ tool synchronization: "something an application keeps a copy of changed". The event names WHAT changed; the
    // message each tool receives is built from the current state when it is sent. Published for every change, wherever it is made.
    public static final String ORGANIZATION_UPSERTED = "OrganizationUpserted";
    public static final String ORG_NODE_UPSERTED = "OrgNodeUpserted";
    public static final String ORG_NODE_DELETED = "OrgNodeDeleted";
    public static final String USER_UPSERTED = "UserUpserted";
    public static final String MEMBERSHIP_CHANGED = "MembershipChanged";
    /** A subscription of an organization changed (any status, date, plan or seats). Not the same as {@link #SUBSCRIPTION_CHANGED}, which keeps its own meaning. */
    public static final String SUBSCRIPTION_SYNCED = "SubscriptionSynced";
    public static final String USER_PRODUCT_ACCESS_GRANTED = "UserProductAccessGranted";
    public static final String USER_PRODUCT_ACCESS_REVOKED = "UserProductAccessRevoked";
    /** An organization with an active subscription for a tool has no tenant in it yet. */
    public static final String TENANT_PROVISIONING_REQUESTED = "TenantProvisioningRequested";

    public static final String AGGREGATE_ORGANIZATION = "Organization";
    public static final String AGGREGATE_ORG_NODE = "OrgNode";
    public static final String AGGREGATE_USER = "User";
    public static final String AGGREGATE_MEMBERSHIP = "Membership";
    public static final String AGGREGATE_USER_ACCESS = "UserAccess";
    public static final String AGGREGATE_TENANT = "Tenant";

    public static final String AGGREGATE_CART = "Cart";
    public static final String AGGREGATE_ORDER = "Order";
    public static final String AGGREGATE_SUBSCRIPTION = "Subscription";
    public static final String AGGREGATE_INVOICE = "Invoice";
    public static final String AGGREGATE_PAYMENT = "Payment";

    private PlatformEventTypes() {
    }
}
