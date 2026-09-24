package com.vyoog.eisplatform.modules.preference.dto;

/** A full replace, not a partial patch — same convention as
 * UpdateDashboardPreferencesRequest, since the frontend always holds the
 * complete current preference set client-side before sending any change. A
 * null field here means "clear it back to unset" (fall back to browser/OS
 * default), not "leave unchanged." */
public record UpdateCustomerPreferenceRequest(
    String language,
    String region,
    String timeZone,
    String themeMode,
    boolean reducedMotion
) {
}
