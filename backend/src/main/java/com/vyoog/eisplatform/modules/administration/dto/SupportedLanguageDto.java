package com.vyoog.eisplatform.modules.administration.dto;

/**
 * 15.01.01 Configure languages (sprint 2026.4.2) — read-only. A language
 * exists here only if this platform actually ships translated strings for
 * it (see frontend {@code i18n/index.ts}'s own SUPPORTED_LANGUAGES); there
 * is no add/remove, since adding a "language" with no translation file
 * behind it would silently show blank or English text with a foreign
 * language selected.
 */
public record SupportedLanguageDto(String code, String label) {
}
