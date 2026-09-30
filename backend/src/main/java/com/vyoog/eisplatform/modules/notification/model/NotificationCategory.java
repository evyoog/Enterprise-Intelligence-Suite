package com.vyoog.eisplatform.modules.notification.model;

/** What kind of real event produced a notification — drives both the
 * notification center's filter chips and per-category email preferences
 * (see NotificationPreference). */
public enum NotificationCategory {
    SECURITY,
    SUBSCRIPTION,
    ORGANIZATION,
    PRIVILEGED_ACCESS,
    SYSTEM,
    /** 09 Order & Provisioning Management (sprint 2027.1.1): order submitted/approved/rejected. */
    ORDER,
    /** 08 Billing & Payments (sprint 2026.4.3, C46): invoice generated, payment received/failed, refund processed. */
    BILLING
}
