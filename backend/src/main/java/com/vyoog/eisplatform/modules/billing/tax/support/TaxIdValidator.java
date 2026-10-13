package com.vyoog.eisplatform.modules.billing.tax.support;

import java.util.Optional;
import java.util.regex.Pattern;

/** TX-4 "Validate tax": checks the FORMAT of a customer's tax ID. India: a 15-character GSTIN whose first two digits are a
 * known state code. Other countries: free text of letters, digits, spaces and . - / up to 50 characters. The GSTIN check
 * digit is not verified (Not specified). */
public final class TaxIdValidator {

    private static final Pattern GSTIN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");
    private static final Pattern GENERIC = Pattern.compile("^[A-Za-z0-9 ./-]{1,50}$");

    private TaxIdValidator() {
    }

    /** Empty when the ID is acceptable, otherwise a message for the user. A blank ID is acceptable. */
    public static Optional<String> check(String countryCode, String taxId) {
        if (taxId == null || taxId.isBlank()) {
            return Optional.empty();
        }
        String id = taxId.trim();
        if ("IN".equals(countryCode)) {
            String upper = id.toUpperCase(java.util.Locale.ROOT);
            if (!GSTIN.matcher(upper).matches()) {
                return Optional.of("Enter a valid 15-character GSTIN, for example 33ABCDE1234F1Z5.");
            }
            if (gstinState(upper).isEmpty()) {
                return Optional.of("The first two digits of the GSTIN are not a valid state code.");
            }
            return Optional.empty();
        }
        return GENERIC.matcher(id).matches() ? Optional.empty()
            : Optional.of("A tax ID may contain letters, digits, spaces and . - / only (up to 50 characters).");
    }

    public static boolean isValidGstin(String taxId) {
        return taxId != null && !taxId.isBlank() && check("IN", taxId).isEmpty();
    }

    /** The state coded in a valid GSTIN. */
    public static Optional<StateCatalog.State> gstinState(String taxId) {
        if (taxId == null || taxId.trim().length() < 2) {
            return Optional.empty();
        }
        String prefix = taxId.trim().substring(0, 2);
        return StateCatalog.statesOf("IN").stream().filter(s -> prefix.equals(s.gstCode())).findFirst();
    }
}
