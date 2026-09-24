package com.vyoog.eisplatform.modules.notification.model;

/** What kind of real event produced a notification — drives both the
 * notification center's filter chips and per-category email preferences
 * (see NotificationPreference). */
public enum NotificationCategory {
    SECURITY,
    SUBSCRIPTION,
    ORGANIZATION,
    PRIVILEGED_ACCESS,
    SYSTEM
}
