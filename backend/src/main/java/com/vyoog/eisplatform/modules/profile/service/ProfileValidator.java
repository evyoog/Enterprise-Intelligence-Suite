package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Address;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Online;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Personal;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Work;
import org.springframework.stereotype.Component;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Server-side source of truth for the My profile form (C107, business rules BR-PRF-*). Returns the
 * normalised request (trimmed, inner whitespace collapsed, blank to null, phones in E.164) or throws a
 * {@link ProfileValidationException} carrying every field error at once.
 *
 * <p>Common rules for free text: trim, collapse inner whitespace, blank becomes null, reject control
 * characters (a tab or line break counts as whitespace and is collapsed) and the characters &lt; and &gt;.
 * URLs are trimmed but never rewritten and may not contain whitespace.
 */
@Component
public class ProfileValidator {

    static final Pattern NAME = Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'\u2019-]{0,99}$");
    static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{6,14}$");
    static final Pattern POSTAL = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 -]{1,10}$");
    static final Pattern INDIA_POSTAL = Pattern.compile("^\\d{6}$");
    private static final Pattern PHONE_SEPARATORS = Pattern.compile("[\\s\\-.()]");
    private static final Pattern HOSTNAME = Pattern.compile(
        "^(?=.{1,253}$)([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$");

    public ProfileUpdateRequest validate(ProfileUpdateRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        Personal personal = request.personal() == null ? new Personal(null, null, null, null, null, null) : request.personal();
        Work work = request.work() == null ? new Work(null, null, null, null) : request.work();
        Address address = request.address() == null ? new Address(null, null, null, null, null, null) : request.address();
        Online online = request.online() == null ? new Online(null, null) : request.online();

        Personal p = new Personal(
            name("personal.firstName", personal.firstName(), 100, true, errors),
            name("personal.lastName", personal.lastName(), 100, true, errors),
            name("personal.preferredName", personal.preferredName(), 60, false, errors),
            phone("personal.mobile", personal.mobile(), errors),
            phone("personal.alternatePhone", personal.alternatePhone(), errors),
            text("personal.bio", personal.bio(), 500, errors));
        Work w = new Work(
            text("work.jobTitle", work.jobTitle(), 100, errors),
            text("work.department", work.department(), 100, errors),
            text("work.companyName", work.companyName(), 150, errors),
            text("work.industry", work.industry(), 100, errors));
        String country = text("address.country", address.country(), 80, errors);
        Address a = new Address(
            text("address.addressLine1", address.addressLine1(), 150, errors),
            text("address.addressLine2", address.addressLine2(), 150, errors),
            text("address.city", address.city(), 80, errors),
            text("address.state", address.state(), 80, errors),
            postalCode("address.postalCode", address.postalCode(), country, errors),
            country);
        Online o = new Online(
            linkedin("online.linkedinUrl", online.linkedinUrl(), errors),
            website("online.websiteUrl", online.websiteUrl(), errors));

        if (!errors.isEmpty()) {
            throw new ProfileValidationException(errors);
        }
        return new ProfileUpdateRequest(p, w, a, o);
    }

    // ---- fields ----

    private String name(String key, String raw, int max, boolean required, Map<String, String> errors) {
        String value = text(key, raw, max, errors);
        if (errors.containsKey(key)) {
            return null;
        }
        if (value == null) {
            if (required) {
                errors.put(key, "This field is required");
            }
            return null;
        }
        if (!NAME.matcher(value).matches()) {
            errors.put(key, "Use letters, spaces and . ' \u2019 - only, starting with a letter");
            return null;
        }
        return value;
    }

    private String phone(String key, String raw, Map<String, String> errors) {
        String cleaned = text(key, raw, 40, errors);
        if (errors.containsKey(key) || cleaned == null) {
            return null;
        }
        String normalised = PHONE_SEPARATORS.matcher(cleaned).replaceAll("");
        if (!E164.matcher(normalised).matches()) {
            errors.put(key, "Enter an international number such as +919876543210 (7 to 15 digits after the +)");
            return null;
        }
        return normalised;
    }

    private String postalCode(String key, String raw, String country, Map<String, String> errors) {
        String value = text(key, raw, 12, errors);
        if (errors.containsKey(key) || value == null) {
            return null;
        }
        if (isIndia(country)) {
            if (!INDIA_POSTAL.matcher(value).matches()) {
                errors.put(key, "A postal code in India has exactly 6 digits");
                return null;
            }
        } else if (!POSTAL.matcher(value).matches()) {
            errors.put(key, "Use 2 to 11 letters, digits, spaces or hyphens, starting with a letter or digit");
            return null;
        }
        return value;
    }

    private static boolean isIndia(String country) {
        return country != null && (country.equalsIgnoreCase("India") || country.equalsIgnoreCase("IN"));
    }

    private String linkedin(String key, String raw, Map<String, String> errors) {
        String value = url(key, raw, errors);
        if (value == null) {
            return null;
        }
        URI uri = parse(value);
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme()) || !validHost(uri)) {
            errors.put(key, "Enter a LinkedIn address starting with https://");
            return null;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (!host.equals("linkedin.com") && !host.endsWith(".linkedin.com")) {
            errors.put(key, "The address must be on linkedin.com");
            return null;
        }
        return value;
    }

    private String website(String key, String raw, Map<String, String> errors) {
        String value = url(key, raw, errors);
        if (value == null) {
            return null;
        }
        URI uri = parse(value);
        boolean web = uri != null && uri.getScheme() != null
            && (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"));
        if (!web || !validHost(uri)) {
            errors.put(key, "Enter a web address starting with http:// or https://");
            return null;
        }
        return value;
    }

    // ---- shared helpers ----

    /** Trim, collapse whitespace, blank to null, reject control characters and angle brackets, enforce a maximum. */
    String text(String key, String raw, int max, Map<String, String> errors) {
        if (raw == null) {
            return null;
        }
        String bad = forbiddenCharacters(raw);
        if (bad != null) {
            errors.put(key, bad);
            return null;
        }
        String value = raw.replaceAll("\\s+", " ").strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > max) {
            errors.put(key, "Must be at most " + max + " characters");
            return null;
        }
        return value;
    }

    private String url(String key, String raw, Map<String, String> errors) {
        if (raw == null) {
            return null;
        }
        String bad = forbiddenCharacters(raw);
        if (bad != null) {
            errors.put(key, bad);
            return null;
        }
        String value = raw.strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 200) {
            errors.put(key, "Must be at most 200 characters");
            return null;
        }
        if (value.chars().anyMatch(Character::isWhitespace)) {
            errors.put(key, "The address cannot contain spaces");
            return null;
        }
        return value;
    }

    /** Null when the text is clean, otherwise the message. Tab, line feed and carriage return are whitespace, not control errors. */
    private static String forbiddenCharacters(String raw) {
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '<' || c == '>') {
                return "The characters < and > are not allowed";
            }
            if (Character.isISOControl(c) && c != '\t' && c != '\n' && c != '\r') {
                return "Control characters are not allowed";
            }
        }
        return null;
    }

    private static URI parse(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /** A real host name (a registrable domain, no IP literal, no credentials in the address). */
    private static boolean validHost(URI uri) {
        String host = uri.getHost();
        if (host == null || uri.getUserInfo() != null) {
            return false;
        }
        try {
            return HOSTNAME.matcher(IDN.toASCII(host)).matches();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
