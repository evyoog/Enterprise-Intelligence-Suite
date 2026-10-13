package com.vyoog.eisplatform.modules.preference.dto;

/**
 * Every field may be null except {@code reducedMotion} — null means "this
 * customer has never set it," which the frontend treats as "use the
 * browser/OS default," never as an empty string or a fabricated default
 * language. See {@link com.vyoog.eisplatform.modules.preference.model.CustomerPreference}'s
 * own javadoc for why language/region/timeZone are three separate fields.
 */
public record CustomerPreferenceDto(
    String language,
    String region,
    String timeZone,
    String themeMode,
    boolean reducedMotion,
    /** C92 (TM-2 A): what the platform gives a user who has not chosen yet; the frontend uses it for every null above. */
    com.vyoog.eisplatform.modules.administration.dto.PlatformDefaultsDto defaults
) {
}
