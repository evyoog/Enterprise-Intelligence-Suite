package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Completion;
import com.vyoog.eisplatform.modules.registration.model.Customer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one computation behind the global "profile filling" bar (C107): eleven equally weighted items,
 * percent = round(filled * 100 / 11). A value counts when non-blank; the photo counts when a photo row exists.
 *
 * <p>This is the PERSON profile. It is NOT the organization-directory completeness (BR-DIR-004/005 in
 * {@code modules.orgdirectory}), which scores a different set of fields and is intentionally left unchanged;
 * aligning the two is a follow-up.
 */
public final class PersonProfileCompleteness {

    public static final List<String> KEYS = List.of("photo", "mobile", "jobTitle", "department", "companyName",
        "industry", "addressLine1", "city", "state", "postalCode", "country");

    private PersonProfileCompleteness() {
    }

    /** Nothing filled: the profile of a signed-in person who has no customer row yet. */
    public static Completion empty() {
        return new Completion(0, 0, KEYS.size(), KEYS);
    }

    public static Completion of(Customer c, boolean hasPhoto) {
        Map<String, Boolean> filled = new LinkedHashMap<>();
        filled.put("photo", hasPhoto);
        filled.put("mobile", present(c.getMobile()));
        filled.put("jobTitle", present(c.getJobTitle()));
        filled.put("department", present(c.getDepartment()));
        filled.put("companyName", present(c.getCompanyName()));
        filled.put("industry", present(c.getIndustry()));
        filled.put("addressLine1", present(c.getAddressLine1()));
        filled.put("city", present(c.getCity()));
        filled.put("state", present(c.getState()));
        filled.put("postalCode", present(c.getPostalCode()));
        filled.put("country", present(c.getCountry()));
        List<String> missing = new ArrayList<>();
        filled.forEach((key, done) -> {
            if (!done) {
                missing.add(key);
            }
        });
        int total = KEYS.size();
        int count = total - missing.size();
        return new Completion((int) Math.round(count * 100.0 / total), count, total, List.copyOf(missing));
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }
}
