package com.vyoog.eisplatform.modules.billing.tax.support;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Turns the free-text country of a billing address ("India", "IN", "IND", "United States") into an ISO 3166-1 alpha-2
 * code. Anything that cannot be resolved returns empty, so a calculation blocks instead of guessing (BR-18). */
public final class CountryResolver {

    private static final Map<String, String> BY_KEY = new HashMap<>();

    static {
        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);
            BY_KEY.put(key(iso), iso);
            try {
                BY_KEY.put(key(locale.getISO3Country()), iso);
            } catch (java.util.MissingResourceException ignored) {
                // no alpha-3 code for this region
            }
            BY_KEY.put(key(locale.getDisplayCountry(Locale.ENGLISH)), iso);
        }
        // Common spellings the JDK does not list.
        BY_KEY.put(key("USA"), "US");
        BY_KEY.put(key("U.S.A."), "US");
        BY_KEY.put(key("United States of America"), "US");
        BY_KEY.put(key("America"), "US");
        BY_KEY.put(key("UK"), "GB");
        BY_KEY.put(key("Great Britain"), "GB");
        BY_KEY.put(key("England"), "GB");
        BY_KEY.put(key("UAE"), "AE");
        BY_KEY.put(key("Republic of India"), "IN");
        BY_KEY.put(key("Bharat"), "IN");
        BY_KEY.put(key("South Korea"), "KR");
        BY_KEY.put(key("Korea, Republic of"), "KR");
        BY_KEY.put(key("Russia"), "RU");
        BY_KEY.put(key("Vietnam"), "VN");
        BY_KEY.put(key("Czech Republic"), "CZ");
    }

    private CountryResolver() {
    }

    public static Optional<String> resolve(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_KEY.get(key(text)));
    }

    /** The English name of an ISO code, or the code itself. */
    public static String nameOf(String iso) {
        if (iso == null) {
            return null;
        }
        String name = new Locale("", iso).getDisplayCountry(Locale.ENGLISH);
        return name == null || name.isBlank() ? iso : name;
    }

    public static boolean isKnownCode(String iso) {
        return iso != null && iso.length() == 2 && java.util.Arrays.asList(Locale.getISOCountries()).contains(iso.toUpperCase(Locale.ROOT));
    }

    static String key(String text) {
        String folded = Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return folded.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
